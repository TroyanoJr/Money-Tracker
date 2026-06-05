package net.micode.spendingtracker.viewmodel

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.micode.spendingtracker.model.Category
import net.micode.spendingtracker.model.Transaction
import net.micode.spendingtracker.repository.TransactionRepository
import net.micode.spendingtracker.util.SettingsManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Supported time periods for transaction filtering and reporting.
 */
enum class Period { DAY, WEEK, MONTH, YEAR }

/**
 * Filter types for the transaction list.
 */
enum class FilterType { ALL, ONLY_EXPENSE, ONLY_INCOME, BY_CATEGORY }

/**
 * Sorting options for transaction records.
 */
enum class SortOrder { DATE_ASC, DATE_DESC, AMOUNT_ASC, AMOUNT_DESC, CATEGORY }

/**
 * Data point for cash flow charts (Income vs Expense over time).
 */
data class CashFlowPoint(val label: String, val income: Double, val expense: Double, val timestamp: Long)

/**
 * Represent a category's contribution to total spending/income in reports.
 */
data class CategoryReportItem(val name: String, val amount: Double, val color: Int, val percentage: Float)

/**
 * Internal helper to manage start and end timestamps for a given period.
 */
private data class DateRange(val start: Long, val end: Long)

/**
 * Main ViewModel for managing transactions, budgets, and financial reports.
 * Uses a reactive approach with StateFlows to ensure the UI stays synchronized with the database.
 */
class TransactionViewModel(
    private val repository: TransactionRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {
    
    /**
     * Event stream triggered whenever transaction data changes in the repository.
     */
    val dataChangedEvent = repository.dataChanged

    private val _selectedPeriod = MutableStateFlow(Period.MONTH)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _selectedAccountId = MutableStateFlow<Long>(1L)
    val selectedAccountId: StateFlow<Long> = _selectedAccountId.asStateFlow()

    private val _currencySymbol = MutableStateFlow(settingsManager.getCurrencySymbol())
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _activeFilterType = MutableStateFlow(FilterType.ALL)
    val activeFilterType: StateFlow<FilterType> = _activeFilterType.asStateFlow()

    private val _filterCategoryName = MutableStateFlow<String?>(null)
    val filterCategoryName: StateFlow<String?> = _filterCategoryName.asStateFlow()

    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery: StateFlow<String?> = _searchQuery.asStateFlow()

    // Budget and Carry Over settings synced from SettingsManager
    private val _isBudgetModeEnabled = MutableStateFlow(false)
    val isBudgetModeEnabled: StateFlow<Boolean> = _isBudgetModeEnabled.asStateFlow()
    private val _monthlyBudget = MutableStateFlow(0.0)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()
    private val _isIncludeIncomeEnabled = MutableStateFlow(false)
    val isIncludeIncomeEnabled: StateFlow<Boolean> = _isIncludeIncomeEnabled.asStateFlow()

    private val _isCarryOverEnabled = MutableStateFlow(false)
    val isCarryOverEnabled: StateFlow<Boolean> = _isCarryOverEnabled.asStateFlow()
    private val _isCarryOverPositiveOnly = MutableStateFlow(false)
    val isCarryOverPositiveOnly: StateFlow<Boolean> = _isCarryOverPositiveOnly.asStateFlow()
    private val _isCarryOverAddToIncome = MutableStateFlow(false)
    val isCarryOverAddToIncome: StateFlow<Boolean> = _isCarryOverAddToIncome.asStateFlow()

    private val _reportIsExpense = MutableStateFlow(true)
    val reportIsExpense: StateFlow<Boolean> = _reportIsExpense.asStateFlow()

    init {
        viewModelScope.launch {
            // Initialize default account and ensure mandatory categories exist
            repository.getDefaultAccount()?.let { _selectedAccountId.value = it.id }
            ensureTransferCategoryExists()
            refreshBudgetSettings()
        }
        // Re-fetch budget settings whenever the active account changes
        _selectedAccountId.onEach { refreshBudgetSettings() }.launchIn(viewModelScope)
    }

    /**
     * Observable list of categories, excluding internal categories like "Transfer".
     * Triggers category seeding if the database is empty.
     */
    val categories: StateFlow<List<Category>> = repository.allCategories
        .map { list -> list.filter { it.name != "Transfer" } }
        .onEach { list ->
            if (list.isEmpty() && !settingsManager.hasSeededCategories()) seedDefaultCategories()
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Observable list of expense categories.
     */
    val expenseCategories = categories.map { l -> l.filter { it.isExpense } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    /**
     * Observable list of income categories.
     */
    val incomeCategories = categories.map { l -> l.filter { !it.isExpense } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Calculates the time window based on current period (e.g., Month, Year) and selected date.
     */
    private val selectedDateRange = combine(_selectedPeriod, _selectedDate) { p, d -> calculateDateRange(p, d) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), calculateDateRange(Period.MONTH, System.currentTimeMillis()))

    /**
     * Source flow for all transactions within the selected period and account.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val periodTransactionsFlow = combine(selectedDateRange, _selectedAccountId) { range, id ->
        range to id
    }.flatMapLatest { (range, id) -> repository.getTransactionsByDateRange(id, range.start, range.end) }

    /**
     * List of all transactions in the current period, used for summary calculations.
     */
    val allTransactionsInPeriod: StateFlow<List<Transaction>> = periodTransactionsFlow
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Filtered version of transactions for display in the main list.
     */
    val transactions: StateFlow<List<Transaction>> = combine(allTransactionsInPeriod, _activeFilterType, _filterCategoryName) { txs, type, cat ->
        when (type) {
            FilterType.ALL -> txs
            FilterType.ONLY_EXPENSE -> txs.filter { it.isExpense }
            FilterType.ONLY_INCOME -> txs.filter { !it.isExpense }
            FilterType.BY_CATEGORY -> if (cat != null) txs.filter { it.categoryName == cat } else txs
        }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Paging-enabled transaction flow for efficient scrolling of large datasets.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedTransactions: Flow<PagingData<Transaction>> = combine(selectedDateRange, _activeFilterType, _filterCategoryName, _searchQuery, _selectedAccountId) { range, type, cat, q, id ->
        val isExp = when (type) { FilterType.ONLY_EXPENSE -> true; FilterType.ONLY_INCOME -> false; else -> null }
        repository.getPagedTransactions(id, range.start, range.end, isExp, if (type == FilterType.BY_CATEGORY) cat else null, q)
    }.flatMapLatest { it }.cachedIn(viewModelScope)

    /** Sets the search query for filtering transactions. */
    fun setSearchQuery(query: String?) { _searchQuery.value = if (query.isNullOrBlank()) null else query }
    
    /** Sets the active filter type and category. */
    fun setFilter(type: FilterType, categoryName: String? = null) { _activeFilterType.value = type; _filterCategoryName.value = categoryName }
    
    /** Updates the currently selected account ID. */
    fun setSelectedAccount(id: Long) { _selectedAccountId.value = id }
    
    /** Refreshes the currency symbol from settings. */
    fun refreshCurrency() { _currencySymbol.value = settingsManager.getCurrencySymbol() }
    
    /** Sets whether reports should focus on expenses or income. */
    fun setReportIsExpense(isExpense: Boolean) { _reportIsExpense.value = isExpense }

    /**
     * Syncs local state with SettingsManager for the specific account.
     * All Accounts (-1L) has its own independent Budget settings but Carry Over remains disabled.
     */
    fun refreshBudgetSettings() {
        val id = _selectedAccountId.value
        _isBudgetModeEnabled.value = settingsManager.isBudgetModeEnabled(id)
        _monthlyBudget.value = settingsManager.getMonthlyBudget(id)
        _isIncludeIncomeEnabled.value = settingsManager.isIncludeIncomeInBudget(id)
        
        if (id != -1L) {
            _isCarryOverEnabled.value = settingsManager.isCarryOverEnabled(id)
            _isCarryOverPositiveOnly.value = settingsManager.isCarryOverPositiveOnly(id)
            _isCarryOverAddToIncome.value = settingsManager.isCarryOverAddToIncome(id)
        } else { 
            // Carry Over is not supported for All Accounts mode
            _isCarryOverEnabled.value = false
            _isCarryOverPositiveOnly.value = false
            _isCarryOverAddToIncome.value = false
        }
    }

    /** Sets the current viewing period (Day, Week, Month, Year). */
    fun setPeriod(period: Period) { _selectedPeriod.value = period }
    
    /** Sets the selected reference date for the current period. */
    fun setDate(dateMillis: Long) { _selectedDate.value = dateMillis }
    
    /** Advances to the next period. */
    fun nextPeriod() { _selectedDate.value = adjustDate(_selectedDate.value, _selectedPeriod.value, 1) }
    
    /** Goes back to the previous period. */
    fun previousPeriod() { _selectedDate.value = adjustDate(_selectedDate.value, _selectedPeriod.value, -1) }

    private fun adjustDate(d: Long, p: Period, a: Int): Long = Calendar.getInstance().apply {
        timeInMillis = d
        when (p) { Period.DAY -> add(Calendar.DAY_OF_YEAR, a); Period.WEEK -> add(Calendar.WEEK_OF_YEAR, a); Period.MONTH -> add(Calendar.MONTH, a); Period.YEAR -> add(Calendar.YEAR, a) }
    }.timeInMillis

    private fun calculateDateRange(p: Period, d: Long): DateRange {
        val s = Calendar.getInstance().apply {
            timeInMillis = d
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            when (p) {
                Period.WEEK -> set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                Period.MONTH -> set(Calendar.DAY_OF_MONTH, 1)
                Period.YEAR -> { set(Calendar.MONTH, Calendar.JANUARY); set(Calendar.DAY_OF_MONTH, 1) }
                else -> Unit
            }
        }
        val e = s.clone() as Calendar
        when (p) {
            Period.DAY -> Unit
            Period.WEEK -> e.add(Calendar.DAY_OF_YEAR, 6)
            Period.MONTH -> e.set(Calendar.DAY_OF_MONTH, e.getActualMaximum(Calendar.DAY_OF_MONTH))
            Period.YEAR -> { e.set(Calendar.MONTH, Calendar.DECEMBER); e.set(Calendar.DAY_OF_MONTH, 31) }
        }
        e.set(Calendar.HOUR_OF_DAY, 23); e.set(Calendar.MINUTE, 59); e.set(Calendar.SECOND, 59); e.set(Calendar.MILLISECOND, 999)
        return DateRange(s.timeInMillis, e.timeInMillis)
    }

    private fun seedDefaultCategories() = viewModelScope.launch {
        val defaults = listOf(
            Category(name = "Food & Dining", iconName = "Restaurant", isExpense = true, color = -0x1bbbd0),
            Category(name = "Shopping", iconName = "ShoppingCart", isExpense = true, color = -0xba3d07),
            Category(name = "Transport", iconName = "DirectionsBus", isExpense = true, color = -0xded60d),
            Category(name = "Utilities & Subs", iconName = "Wifi", isExpense = true, color = -0xff6978),
            Category(name = "Health & Beauty", iconName = "HealthAndSafety", isExpense = true, color = -0xb550b),
            Category(name = "Entertainment", iconName = "Movie", isExpense = true, color = -0x543cb6),
            Category(name = "Education", iconName = "School", isExpense = true, color = -0xff9678),
            Category(name = "Finance & Social", iconName = "Payments", isExpense = true, color = -0x86aab8),
            Category(name = "Transfer", iconName = "SyncAlt", isExpense = true, color = -0x1000000)
        )
        defaults.forEach { repository.insertCategory(it) }
        settingsManager.setHasSeededCategories(true)
    }

    private fun ensureTransferCategoryExists() = viewModelScope.launch { repository.insertCategory(Category(name = "Transfer", iconName = "SyncAlt", isExpense = true, color = -0x1000000)) }

    /**
     * Transfers funds from one account to another by creating matching expense and income transactions.
     */
    fun transferFunds(fromAccountId: Long, toAccountId: Long, amount: Double, date: Long, note: String) = viewModelScope.launch {
        val uuid = UUID.randomUUID().toString()
        repository.insertTransaction(Transaction(id = "${uuid}_out", amount = amount, categoryName = "Transfer", date = date, note = note, isExpense = true, accountId = fromAccountId))
        repository.insertTransaction(Transaction(id = "${uuid}_in", amount = amount, categoryName = "Transfer", date = date, note = note, isExpense = false, accountId = toAccountId))
    }

    /**
     * Sum of all expenses in the current period.
     */
    val totalExpense = allTransactionsInPeriod.map { it.filter { t -> t.isExpense }.sumOf { t -> t.amount } }.distinctUntilChanged().flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    private val carryOverSettings = combine(_isCarryOverEnabled, _isCarryOverPositiveOnly) { enabled, posOnly -> enabled to posOnly }
    private val budgetBasicSettings = combine(_isBudgetModeEnabled, _monthlyBudget, _isIncludeIncomeEnabled) { enabled, budget, includeIncome -> 
        Triple(enabled, budget, includeIncome) 
    }

    /**
     * Core logic for calculating the amount carried over from previous months.
     * In Standard Mode: Returns the total historical balance before the current period.
     * In Budget Mode: Returns the cumulative difference between effective budget and actual spending.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val carryOverAmount: StateFlow<Double> = combine(
        _selectedAccountId,
        selectedDateRange,
        carryOverSettings,
        budgetBasicSettings,
        dataChangedEvent.onStart { emit(Unit) }
    ) { id, range, cSettings, bSettings, _ ->
        val (enabled, posOnly) = cSettings
        val (budgetEnabled, mBudget, includeIncome) = bSettings
        
        if (!enabled || id == -1L) return@combine 0.0
        
        if (budgetEnabled) {
            // Budget carry over: cumulative (Effective Budget) - (Expenses since the beginning)
            val firstExpenseDate = repository.getFirstExpenseDate(id)
            if (firstExpenseDate == null) {
                0.0
            } else {
                val calStart = Calendar.getInstance().apply { 
                    timeInMillis = firstExpenseDate 
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                val calCurrent = Calendar.getInstance().apply { 
                    timeInMillis = range.start 
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val monthsDiff = (calCurrent.get(Calendar.YEAR) - calStart.get(Calendar.YEAR)) * 12 +
                        (calCurrent.get(Calendar.MONTH) - calStart.get(Calendar.MONTH))
                
                if (monthsDiff <= 0) {
                    0.0
                } else {
                    val totalExpensesBefore = repository.getTotalExpensesBeforeDate(id, range.start)
                    val cumulativeFixedBudget = mBudget * monthsDiff
                    
                    // FIXED: If income is included in budget, add historical income to the cumulative budget
                    val totalIncomeBefore = if (includeIncome) repository.getTotalIncomeBeforeDate(id, range.start) else 0.0
                    
                    val carry = (cumulativeFixedBudget + totalIncomeBefore) - totalExpensesBefore
                    if (posOnly && carry < 0.0) 0.0 else carry
                }
            }
        } else {
            // Standard carry over: simple balance summation up to the start of current period
            val previousBalance = repository.getBalanceBeforeDate(id, range.start)
            if (posOnly && previousBalance < 0.0) 0.0 else previousBalance
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /**
     * The effective budget for the month, including any carried over savings/debt.
     */
    val dynamicBudget: StateFlow<Double> = combine(
        _monthlyBudget,
        carryOverAmount,
        _isBudgetModeEnabled,
        _isCarryOverEnabled
    ) { fixed, carry, budgetEnabled, carryEnabled ->
        if (budgetEnabled && carryEnabled) fixed + carry else fixed
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /**
     * Total income for the period. 
     * If 'Carry Over Add to Income' is true, it incorporates the historical balance into this figure.
     */
    val totalIncome = combine(
        allTransactionsInPeriod.map { it.filter { t -> !t.isExpense }.sumOf { t -> t.amount } },
        carryOverAmount,
        _isCarryOverAddToIncome,
        _isBudgetModeEnabled,
        _isCarryOverEnabled
    ) { income, carry, add, budgetEnabled, carryEnabled ->
        if (add && !(budgetEnabled && carryEnabled)) income + carry else income
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val fullBudgetSettings = combine(_isBudgetModeEnabled, _isIncludeIncomeEnabled, dynamicBudget) { enabled, includeIncome, dBudget ->
        Triple(enabled, includeIncome, dBudget)
    }

    /**
     * Final calculation of the current period's financial state.
     * In Standard Mode: Net profit/loss + Carry Over (if not already in totalIncome).
     * In Budget Mode: Remaining funds relative to the dynamic budget limit.
     */
    val balance = combine(
        totalIncome, 
        totalExpense, 
        carryOverAmount, 
        _isCarryOverAddToIncome, 
        fullBudgetSettings
    ) { income, expense, carry, add, bSettings ->
        val (budgetEnabled, includeIncome, dBudget) = bSettings

        if (budgetEnabled) {
            val base = if (includeIncome) dBudget + income else dBudget
            base - expense
        } else {
            val net = income - expense
            // If carry wasn't added to totalIncome for display, we add it here 
            // to maintain a cumulative historical balance (Cumulative Flow Logic).
            if (!add) net + carry else net
        }
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /**
     * Aggregated expenses grouped by category for the summary chart.
     */
    val expensesByCategory = allTransactionsInPeriod.map { list ->
        list.filter { it.isExpense }.groupBy { it.categoryName }.map { (n, ts) -> n to ts.sumOf { it.amount } }.sortedByDescending { it.second }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Daily balance changes used to populate the Heatmap calendar.
     */
    val heatmapData = combine(selectedDateRange, allTransactionsInPeriod) { range, list ->
        val map = mutableMapOf<Long, Double>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = range.start
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        while (cal.timeInMillis <= range.end) {
            map[cal.timeInMillis] = 0.0
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list.forEach { tx ->
            cal.timeInMillis = tx.date
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            val day = cal.timeInMillis
            if (map.containsKey(day)) map[day] = (map[day] ?: 0.0) + (if (tx.isExpense) -tx.amount else tx.amount)
        }
        map
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /**
     * Count of transactions that are marked as incomplete (e.g., pending).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val incompleteTransactionsCount = _selectedAccountId.flatMapLatest { id ->
        repository.getAllTransactions(id).map { l -> l.count { !it.isComplete } }
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * Detailed timeline data for the Cash Flow chart (Bar Chart).
     */
    val cashFlowData: StateFlow<List<CashFlowPoint>> = combine(transactions, _selectedPeriod, _selectedDate) { list, period, date ->
        val cal = Calendar.getInstance().apply { timeInMillis = date }
        val points = mutableListOf<CashFlowPoint>()
        val txCal = Calendar.getInstance()
        val grouped = list.groupBy { 
            txCal.timeInMillis = it.date
            when (period) {
                Period.DAY -> txCal.get(Calendar.HOUR_OF_DAY)
                Period.WEEK -> txCal.get(Calendar.DAY_OF_WEEK)
                Period.MONTH -> txCal.get(Calendar.DAY_OF_MONTH)
                Period.YEAR -> txCal.get(Calendar.MONTH)
            }
        }
        when (period) {
            Period.DAY -> {
                val sdf = SimpleDateFormat("HH:00", Locale.getDefault())
                for (h in 0..23) {
                    val ts = grouped[h] ?: emptyList()
                    cal.set(Calendar.HOUR_OF_DAY, h); points.add(CashFlowPoint(sdf.format(cal.time), ts.filter { !it.isExpense }.sumOf { it.amount }, ts.filter { it.isExpense }.sumOf { it.amount }, cal.timeInMillis))
                }
            }
            Period.WEEK -> {
                val sdf = SimpleDateFormat("EEE", Locale.getDefault())
                val temp = cal.clone() as Calendar; temp.set(Calendar.DAY_OF_WEEK, temp.firstDayOfWeek)
                for (i in 0..6) {
                    val day = temp.get(Calendar.DAY_OF_WEEK)
                    val ts = grouped[day] ?: emptyList()
                    points.add(CashFlowPoint(sdf.format(temp.time), ts.filter { !it.isExpense }.sumOf { it.amount }, ts.filter { it.isExpense }.sumOf { it.amount }, temp.timeInMillis))
                    temp.add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            Period.MONTH -> {
                val sdf = SimpleDateFormat("d", Locale.getDefault())
                for (d in 1..cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    val ts = grouped[d] ?: emptyList()
                    cal.set(Calendar.DAY_OF_MONTH, d); points.add(CashFlowPoint(sdf.format(cal.time), ts.filter { !it.isExpense }.sumOf { it.amount }, ts.filter { it.isExpense }.sumOf { it.amount }, cal.timeInMillis))
                }
            }
            Period.YEAR -> {
                val sdf = SimpleDateFormat("MMM", Locale.getDefault())
                for (m in 0..11) {
                    val ts = grouped[m] ?: emptyList()
                    cal.set(Calendar.MONTH, m); points.add(CashFlowPoint(sdf.format(cal.time), ts.filter { !it.isExpense }.sumOf { it.amount }, ts.filter { it.isExpense }.sumOf { it.amount }, cal.timeInMillis))
                }
            }
        }
        points
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Percentage-based data for the Category Pie/List report.
     */
    val categoryReportData = combine(allTransactionsInPeriod, categories, _reportIsExpense) { txs, cats, isExp ->
        val filtered = txs.filter { it.isExpense == isExp }
        val total = filtered.sumOf { it.amount }
        filtered.groupBy { it.categoryName }.map { (n, ts) ->
            val amt = ts.sumOf { it.amount }
            val c = cats.find { it.name == n }
            CategoryReportItem(n, amt, c?.color ?: -0x1, if (total > 0) (amt / total).toFloat() else 0f)
        }.sortedByDescending { it.amount }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Adds a new transaction to the database. */
    fun addTransaction(transaction: Transaction) = viewModelScope.launch {
        val aid = if (_selectedAccountId.value == -1L) 1L else _selectedAccountId.value
        repository.insertTransaction(if (transaction.accountId <= 0L) transaction.copy(accountId = aid) else transaction)
    }
    
    /** Updates an existing transaction. */
    fun updateTransaction(transaction: Transaction) { viewModelScope.launch { repository.updateTransaction(transaction) } }
    
    /** Deletes a single transaction. */
    fun deleteTransaction(transaction: Transaction) { viewModelScope.launch { repository.deleteTransaction(transaction) } }
    
    /** Deletes a list of transactions. */
    fun deleteTransactions(transactions: List<Transaction>) { viewModelScope.launch { transactions.forEach { repository.deleteTransaction(it) } } }
    
    /** Adds a new category. */
    fun addCategory(category: Category) { viewModelScope.launch { repository.insertCategory(category) } }
    
    /** Updates an existing category. */
    fun updateCategory(category: Category) { viewModelScope.launch { repository.updateCategory(category) } }
    
    /** Deletes a list of categories. */
    fun deleteCategories(categories: List<Category>) { viewModelScope.launch { repository.deleteCategories(categories) } }
    
    /** Generates a CSV string of transactions for export (Stub). */
    suspend fun generateCsvString(s: Long, e: Long, n: Boolean, c: String, so: String, se: String): String = ""
}
