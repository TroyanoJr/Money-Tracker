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

enum class Period { DAY, WEEK, MONTH, YEAR }
enum class FilterType { ALL, ONLY_EXPENSE, ONLY_INCOME, BY_CATEGORY }
enum class SortOrder { DATE_ASC, DATE_DESC, AMOUNT_ASC, AMOUNT_DESC, CATEGORY }

data class CashFlowPoint(val label: String, val income: Double, val expense: Double, val timestamp: Long)
data class CategoryReportItem(val name: String, val amount: Double, val color: Int, val percentage: Float)
private data class DateRange(val start: Long, val end: Long)

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {
    
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
            repository.getDefaultAccount()?.let { _selectedAccountId.value = it.id }
            ensureTransferCategoryExists()
            refreshBudgetSettings()
        }
        _selectedAccountId.onEach { refreshBudgetSettings() }.launchIn(viewModelScope)
    }

    val categories: StateFlow<List<Category>> = repository.allCategories
        .map { list -> list.filter { it.name != "Transfer" } }
        .onEach { list ->
            if (list.isEmpty() && !settingsManager.hasSeededCategories()) seedDefaultCategories()
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories = categories.map { l -> l.filter { it.isExpense } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val incomeCategories = categories.map { l -> l.filter { !it.isExpense } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val selectedDateRange = combine(_selectedPeriod, _selectedDate) { p, d -> calculateDateRange(p, d) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), calculateDateRange(Period.MONTH, System.currentTimeMillis()))

    @OptIn(ExperimentalCoroutinesApi::class)
    private val periodTransactionsFlow = combine(selectedDateRange, _selectedAccountId) { range, id ->
        range to id
    }.flatMapLatest { (range, id) -> repository.getTransactionsByDateRange(id, range.start, range.end) }

    val allTransactionsInPeriod: StateFlow<List<Transaction>> = periodTransactionsFlow
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = combine(allTransactionsInPeriod, _activeFilterType, _filterCategoryName) { txs, type, cat ->
        when (type) {
            FilterType.ALL -> txs
            FilterType.ONLY_EXPENSE -> txs.filter { it.isExpense }
            FilterType.ONLY_INCOME -> txs.filter { !it.isExpense }
            FilterType.BY_CATEGORY -> if (cat != null) txs.filter { it.categoryName == cat } else txs
        }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedTransactions: Flow<PagingData<Transaction>> = combine(selectedDateRange, _activeFilterType, _filterCategoryName, _searchQuery, _selectedAccountId) { range, type, cat, q, id ->
        val isExp = when (type) { FilterType.ONLY_EXPENSE -> true; FilterType.ONLY_INCOME -> false; else -> null }
        repository.getPagedTransactions(id, range.start, range.end, isExp, if (type == FilterType.BY_CATEGORY) cat else null, q)
    }.flatMapLatest { it }.cachedIn(viewModelScope)

    fun setSearchQuery(query: String?) { _searchQuery.value = if (query.isNullOrBlank()) null else query }
    fun setFilter(type: FilterType, categoryName: String? = null) { _activeFilterType.value = type; _filterCategoryName.value = categoryName }
    fun setSelectedAccount(id: Long) { _selectedAccountId.value = id }
    fun refreshCurrency() { _currencySymbol.value = settingsManager.getCurrencySymbol() }
    fun setReportIsExpense(isExpense: Boolean) { _reportIsExpense.value = isExpense }

    fun refreshBudgetSettings() {
        val id = _selectedAccountId.value
        if (id != -1L) {
            _isBudgetModeEnabled.value = settingsManager.isBudgetModeEnabled(id)
            _monthlyBudget.value = settingsManager.getMonthlyBudget(id)
            _isIncludeIncomeEnabled.value = settingsManager.isIncludeIncomeInBudget(id)
            
            _isCarryOverEnabled.value = settingsManager.isCarryOverEnabled(id)
            _isCarryOverPositiveOnly.value = settingsManager.isCarryOverPositiveOnly(id)
            _isCarryOverAddToIncome.value = settingsManager.isCarryOverAddToIncome(id)
        } else { 
            _isBudgetModeEnabled.value = false 
            _isCarryOverEnabled.value = false
        }
    }

    fun setPeriod(period: Period) { _selectedPeriod.value = period }
    fun setDate(dateMillis: Long) { _selectedDate.value = dateMillis }
    fun nextPeriod() { _selectedDate.value = adjustDate(_selectedDate.value, _selectedPeriod.value, 1) }
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

    fun transferFunds(fromAccountId: Long, toAccountId: Long, amount: Double, date: Long, note: String) = viewModelScope.launch {
        val uuid = UUID.randomUUID().toString()
        repository.insertTransaction(Transaction(id = "${uuid}_out", amount = amount, categoryName = "Transfer", date = date, note = note, isExpense = true, accountId = fromAccountId))
        repository.insertTransaction(Transaction(id = "${uuid}_in", amount = amount, categoryName = "Transfer", date = date, note = note, isExpense = false, accountId = toAccountId))
    }

    val totalExpense = allTransactionsInPeriod.map { it.filter { t -> t.isExpense }.sumOf { t -> t.amount } }.distinctUntilChanged().flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    private val carryOverSettings = combine(_isCarryOverEnabled, _isCarryOverPositiveOnly) { enabled, posOnly -> enabled to posOnly }
    private val budgetBasicSettings = combine(_isBudgetModeEnabled, _monthlyBudget, _isIncludeIncomeEnabled) { enabled, budget, includeIncome -> 
        Triple(enabled, budget, includeIncome) 
    }

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
            val oldestTxDate = repository.getOldestTransactionDate(id)
            if (oldestTxDate == null) {
                0.0
            } else {
                val calStart = Calendar.getInstance().apply { 
                    timeInMillis = oldestTxDate 
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
                    val cumulativeFixedBudget = mBudget * monthsDiff
                    val carry = if (includeIncome) {
                        cumulativeFixedBudget + repository.getBalanceBeforeDate(id, range.start)
                    } else {
                        cumulativeFixedBudget - repository.getTotalExpensesBeforeDate(id, range.start)
                    }
                    
                    if (posOnly && carry < 0.0) 0.0 else carry
                }
            }
        } else {
            val previousBalance = repository.getBalanceBeforeDate(id, range.start)
            if (posOnly && previousBalance < 0.0) 0.0 else previousBalance
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dynamicBudget: StateFlow<Double> = combine(
        _monthlyBudget,
        carryOverAmount,
        _isBudgetModeEnabled,
        _isCarryOverEnabled
    ) { fixed, carry, budgetEnabled, carryEnabled ->
        if (budgetEnabled && carryEnabled) fixed + carry else fixed
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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

    val balance = combine(totalIncome, totalExpense, fullBudgetSettings) { income, expense, bSettings ->
        val (budgetEnabled, includeIncome, dBudget) = bSettings

        if (budgetEnabled) {
            val base = if (includeIncome) dBudget + income else dBudget
            base - expense
        } else {
            income - expense
        }
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val expensesByCategory = allTransactionsInPeriod.map { list ->
        list.filter { it.isExpense }.groupBy { it.categoryName }.map { (n, ts) -> n to ts.sumOf { it.amount } }.sortedByDescending { it.second }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    @OptIn(ExperimentalCoroutinesApi::class)
    val incompleteTransactionsCount = _selectedAccountId.flatMapLatest { id ->
        repository.getAllTransactions(id).map { l -> l.count { !it.isComplete } }
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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

    val categoryReportData = combine(allTransactionsInPeriod, categories, _reportIsExpense) { txs, cats, isExp ->
        val filtered = txs.filter { it.isExpense == isExp }
        val total = filtered.sumOf { it.amount }
        filtered.groupBy { it.categoryName }.map { (n, ts) ->
            val amt = ts.sumOf { it.amount }
            val c = cats.find { it.name == n }
            CategoryReportItem(n, amt, c?.color ?: -0x1, if (total > 0) (amt / total).toFloat() else 0f)
        }.sortedByDescending { it.amount }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTransaction(transaction: Transaction) = viewModelScope.launch {
        val aid = if (_selectedAccountId.value == -1L) 1L else _selectedAccountId.value
        repository.insertTransaction(if (transaction.accountId <= 0L) transaction.copy(accountId = aid) else transaction)
    }
    fun updateTransaction(transaction: Transaction) { viewModelScope.launch { repository.updateTransaction(transaction) } }
    fun deleteTransaction(transaction: Transaction) { viewModelScope.launch { repository.deleteTransaction(transaction) } }
    fun deleteTransactions(transactions: List<Transaction>) { viewModelScope.launch { transactions.forEach { repository.deleteTransaction(it) } } }
    fun addCategory(category: Category) { viewModelScope.launch { repository.insertCategory(category) } }
    fun updateCategory(category: Category) { viewModelScope.launch { repository.updateCategory(category) } }
    fun deleteCategories(categories: List<Category>) { viewModelScope.launch { repository.deleteCategories(categories) } }
    suspend fun generateCsvString(s: Long, e: Long, n: Boolean, c: String, so: String, se: String): String = ""
}
