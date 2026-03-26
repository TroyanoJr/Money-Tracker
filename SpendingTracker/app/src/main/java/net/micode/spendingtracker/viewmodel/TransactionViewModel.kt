package net.micode.spendingtracker.viewmodel

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.micode.spendingtracker.model.Category
import net.micode.spendingtracker.model.PeriodSummary
import net.micode.spendingtracker.model.Transaction
import net.micode.spendingtracker.repository.TransactionRepository
import net.micode.spendingtracker.util.SettingsManager
import java.text.SimpleDateFormat
import java.util.*

enum class Period {
    DAY, WEEK, MONTH, YEAR
}

enum class FilterType {
    ALL, ONLY_EXPENSE, ONLY_INCOME, BY_CATEGORY
}

enum class SortOrder {
    DATE_ASC, DATE_DESC, AMOUNT_ASC, AMOUNT_DESC, CATEGORY
}

data class CashFlowPoint(
    val label: String,
    val income: Double,
    val expense: Double,
    val timestamp: Long
)

data class CategoryReportItem(
    val name: String,
    val amount: Double,
    val color: Int,
    val percentage: Float
)

private data class DateRange(
    val start: Long,
    val end: Long
)

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {
    private data class PagingFilter(
        val startDate: Long,
        val endDate: Long,
        val isExpense: Boolean?,
        val categoryName: String?,
        val searchQuery: String?
    )

    private val _selectedPeriod = MutableStateFlow(Period.MONTH)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _currencySymbol = MutableStateFlow(settingsManager.getCurrencySymbol())
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _activeFilterType = MutableStateFlow(FilterType.ALL)
    val activeFilterType: StateFlow<FilterType> = _activeFilterType.asStateFlow()

    private val _filterCategoryName = MutableStateFlow<String?>(null)
    val filterCategoryName: StateFlow<String?> = _filterCategoryName.asStateFlow()

    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery: StateFlow<String?> = _searchQuery.asStateFlow()

    // Budget States
    private val _isBudgetModeEnabled = MutableStateFlow(settingsManager.isBudgetModeEnabled())
    val isBudgetModeEnabled: StateFlow<Boolean> = _isBudgetModeEnabled.asStateFlow()

    private val _monthlyBudget = MutableStateFlow(settingsManager.getMonthlyBudget())
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private val _isIncludeIncomeEnabled = MutableStateFlow(settingsManager.isIncludeIncomeInBudget())
    val isIncludeIncomeEnabled: StateFlow<Boolean> = _isIncludeIncomeEnabled.asStateFlow()

    val categories: StateFlow<List<Category>> = repository.allCategories
        .onEach { list ->
            // Seed defaults AND ensure "Pending" always exists
            if (list.isEmpty() && !settingsManager.hasSeededCategories()) {
                seedDefaultCategories()
            } else if (list.none { it.name == "Pending" }) {
                ensurePendingCategoryExists()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val expenseCategories: StateFlow<List<Category>> = categories
        .map { list -> list.filter { it.isExpense } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<Category>> = categories
        .map { list -> list.filter { !it.isExpense } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val selectedDateRange: StateFlow<DateRange> = combine(
        _selectedPeriod,
        _selectedDate
    ) { period, date ->
        calculateDateRange(period, date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = calculateDateRange(_selectedPeriod.value, _selectedDate.value)
    )

    private val periodTransactionsFlow: Flow<List<Transaction>> = selectedDateRange
        .flatMapLatest { range ->
            repository.getTransactionsByDateRange(range.start, range.end)
        }

    val transactions: StateFlow<List<Transaction>> = combine(
        periodTransactionsFlow,
        _activeFilterType,
        _filterCategoryName
    ) { periodTransactions, filterType, catName ->
        when (filterType) {
            FilterType.ALL -> periodTransactions
            FilterType.ONLY_EXPENSE -> periodTransactions.filter { it.isExpense }
            FilterType.ONLY_INCOME -> periodTransactions.filter { !it.isExpense }
            FilterType.BY_CATEGORY -> if (catName != null) {
                periodTransactions.filter { it.categoryName == catName }
            } else periodTransactions
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val pagingFilter: Flow<PagingFilter> = combine(
        selectedDateRange,
        _activeFilterType,
        _filterCategoryName,
        _searchQuery
    ) { range, filterType, categoryName, query ->
        val isExpense = when (filterType) {
            FilterType.ONLY_EXPENSE -> true
            FilterType.ONLY_INCOME -> false
            else -> null
        }
        PagingFilter(range.start, range.end, isExpense, if (filterType == FilterType.BY_CATEGORY) categoryName else null, query)
    }.distinctUntilChanged()

    val pagedTransactions: Flow<PagingData<Transaction>> = pagingFilter
        .flatMapLatest { filter ->
            repository.getPagedTransactions(
                startDate = filter.startDate,
                endDate = filter.endDate,
                isExpense = filter.isExpense,
                categoryName = filter.categoryName,
                searchQuery = filter.searchQuery
            )
        }
        .cachedIn(viewModelScope)

    fun setSearchQuery(query: String?) {
        _searchQuery.value = if (query.isNullOrBlank()) null else query
    }

    fun setFilter(type: FilterType, categoryName: String? = null) {
        _activeFilterType.value = type
        _filterCategoryName.value = categoryName
    }

    fun refreshCurrency() {
        _currencySymbol.value = settingsManager.getCurrencySymbol()
    }

    fun refreshBudgetSettings() {
        _isBudgetModeEnabled.value = settingsManager.isBudgetModeEnabled()
        _monthlyBudget.value = settingsManager.getMonthlyBudget()
        _isIncludeIncomeEnabled.value = settingsManager.isIncludeIncomeInBudget()
    }

    fun setPeriod(period: Period) { _selectedPeriod.value = period }
    fun setDate(dateMillis: Long) { _selectedDate.value = dateMillis }
    fun nextPeriod() { _selectedDate.value = adjustDate(_selectedDate.value, _selectedPeriod.value, 1) }
    fun previousPeriod() { _selectedDate.value = adjustDate(_selectedDate.value, _selectedPeriod.value, -1) }

    private fun adjustDate(currentDate: Long, period: Period, amount: Int): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
        when (period) {
            Period.DAY -> calendar.add(Calendar.DAY_OF_YEAR, amount)
            Period.WEEK -> calendar.add(Calendar.WEEK_OF_YEAR, amount)
            Period.MONTH -> calendar.add(Calendar.MONTH, amount)
            Period.YEAR -> calendar.add(Calendar.YEAR, amount)
        }
        return calendar.timeInMillis
    }

    private fun calculateDateRange(period: Period, dateMillis: Long): DateRange {
        val startCalendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val endCalendar = startCalendar.clone() as Calendar
        when (period) {
            Period.WEEK -> { 
                startCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                endCalendar.timeInMillis = startCalendar.timeInMillis
                endCalendar.add(Calendar.DAY_OF_WEEK, 6) 
            }
            Period.MONTH -> { 
                startCalendar.set(Calendar.DAY_OF_MONTH, 1)
                endCalendar.timeInMillis = startCalendar.timeInMillis
                endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)) 
            }
            Period.YEAR -> { 
                startCalendar.set(Calendar.MONTH, Calendar.JANUARY)
                startCalendar.set(Calendar.DAY_OF_MONTH, 1)
                endCalendar.timeInMillis = startCalendar.timeInMillis
                endCalendar.set(Calendar.MONTH, Calendar.DECEMBER)
                endCalendar.set(Calendar.DAY_OF_MONTH, 31) 
            }
            else -> Unit
        }
        endCalendar.set(Calendar.HOUR_OF_DAY, 23); endCalendar.set(Calendar.MINUTE, 59); endCalendar.set(Calendar.SECOND, 59); endCalendar.set(Calendar.MILLISECOND, 999)
        return DateRange(start = startCalendar.timeInMillis, end = endCalendar.timeInMillis)
    }

    private fun seedDefaultCategories() {
        viewModelScope.launch {
            val defaults = listOf(
                Category(name = "Food & Dining", iconName = "Restaurant", isExpense = true, color = -0x1bbbd0),
                Category(name = "Shopping", iconName = "ShoppingCart", isExpense = true, color = -0xba3d07),
                Category(name = "Transport", iconName = "DirectionsBus", isExpense = true, color = -0xded60d),
                Category(name = "Utilities & Subs", iconName = "Wifi", isExpense = true, color = -0xff6978),
                Category(name = "Health & Beauty", iconName = "HealthAndSafety", isExpense = true, color = -0xb550b),
                Category(name = "Entertainment", iconName = "Movie", isExpense = true, color = -0x543cb6),
                Category(name = "Education", iconName = "School", isExpense = true, color = -0xff9678),
                Category(name = "Finance & Social", iconName = "Payments", isExpense = true, color = -0x86aab8),
                Category(name = "Pending", iconName = "Sell", isExpense = true, color = -0x616162)
            )
            defaults.forEach { repository.insertCategory(it) }
            settingsManager.setHasSeededCategories(true)
        }
    }

    private fun ensurePendingCategoryExists() {
        viewModelScope.launch {
            repository.insertCategory(
                Category(name = "Pending", iconName = "Sell", isExpense = true, color = -0x616162)
            )
        }
    }

    val totalExpense: StateFlow<Double> = transactions.map { list -> list.filter { it.isExpense }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIncome: StateFlow<Double> = transactions.map { list -> list.filter { !it.isExpense }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val balance: StateFlow<Double> = combine(totalIncome, totalExpense) { income, expense -> income - expense }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val expensesByCategory: StateFlow<List<Pair<String, Double>>> = transactions.map { list ->
        list.filter { it.isExpense }
            .groupBy { it.categoryName }
            .map { (name, txs) -> name to txs.sumOf { it.amount } }
            .sortedByDescending { it.second }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val heatmapData: StateFlow<Map<Long, Double>> = combine(selectedDateRange, transactions) { range, list ->
        val map = mutableMapOf<Long, Double>()
        val cal = Calendar.getInstance().apply {
            timeInMillis = range.start
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        while (cal.timeInMillis <= range.end) {
            map[cal.timeInMillis] = 0.0
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list.forEach { tx ->
            val dayStart = Calendar.getInstance().apply {
                timeInMillis = tx.date
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            if (map.containsKey(dayStart)) {
                map[dayStart] = (map[dayStart] ?: 0.0) + (if (tx.isExpense) -tx.amount else tx.amount)
            }
        }
        map
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val incompleteTransactionsCount: StateFlow<Int> = repository.allTransactions.map { list ->
        list.count { !it.isComplete }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cashFlowData: StateFlow<List<CashFlowPoint>> = combine(transactions, _selectedPeriod, _selectedDate) { list, period, date ->
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
        val points = mutableListOf<CashFlowPoint>()
        when (period) {
            Period.DAY -> {
                val sdf = SimpleDateFormat("HH:00", Locale.getDefault())
                val grouped = list.groupBy { Calendar.getInstance().apply { timeInMillis = it.date }.get(Calendar.HOUR_OF_DAY) }
                for (hour in 0..23) {
                    val txs = grouped[hour] ?: emptyList()
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    points.add(CashFlowPoint(sdf.format(calendar.time), txs.filter { !it.isExpense }.sumOf { it.amount }, txs.filter { it.isExpense }.sumOf { it.amount }, calendar.timeInMillis))
                }
            }
            Period.WEEK -> {
                val sdf = SimpleDateFormat("EEE", Locale.getDefault())
                val grouped = list.groupBy { Calendar.getInstance().apply { timeInMillis = it.date }.get(Calendar.DAY_OF_WEEK) }
                val tempCal = calendar.clone() as Calendar
                tempCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                for (i in 0..6) {
                    val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
                    val txs = grouped[dayOfWeek] ?: emptyList()
                    points.add(CashFlowPoint(sdf.format(tempCal.time), txs.filter { !it.isExpense }.sumOf { it.amount }, txs.filter { it.isExpense }.sumOf { it.amount }, tempCal.timeInMillis))
                    tempCal.add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            Period.MONTH -> {
                val sdf = SimpleDateFormat("d", Locale.getDefault())
                val grouped = list.groupBy { Calendar.getInstance().apply { timeInMillis = it.date }.get(Calendar.DAY_OF_MONTH) }
                val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                for (day in 1..maxDay) {
                    val txs = grouped[day] ?: emptyList()
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    points.add(CashFlowPoint(sdf.format(calendar.time), txs.filter { !it.isExpense }.sumOf { it.amount }, txs.filter { it.isExpense }.sumOf { it.amount }, calendar.timeInMillis))
                }
            }
            Period.YEAR -> {
                val sdf = SimpleDateFormat("MMM", Locale.getDefault())
                val grouped = list.groupBy { Calendar.getInstance().apply { timeInMillis = it.date }.get(Calendar.MONTH) }
                for (month in 0..11) {
                    val txs = grouped[month] ?: emptyList()
                    calendar.set(Calendar.MONTH, month)
                    points.add(CashFlowPoint(sdf.format(calendar.time), txs.filter { !it.isExpense }.sumOf { it.amount }, txs.filter { it.isExpense }.sumOf { it.amount }, calendar.timeInMillis))
                }
            }
        }
        points
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryReportData: StateFlow<List<CategoryReportItem>> = combine(transactions, categories) { txList, catList ->
        val expenses = txList.filter { it.isExpense }
        val total = expenses.sumOf { it.amount }
        expenses.groupBy { it.categoryName }
            .map { (name, txs) ->
                val amount = txs.sumOf { it.amount }
                val cat = catList.find { it.name == name }
                CategoryReportItem(name, amount, cat?.color ?: -0x1, if (total > 0) (amount / total).toFloat() else 0f)
            }.sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTransaction(transaction: Transaction) { viewModelScope.launch { repository.insertTransaction(transaction) } }
    fun updateTransaction(transaction: Transaction) { viewModelScope.launch { repository.updateTransaction(transaction) } }
    fun deleteTransaction(transaction: Transaction) { viewModelScope.launch { repository.deleteTransaction(transaction) } }
    fun deleteTransactions(transactions: List<Transaction>) { viewModelScope.launch { transactions.forEach { repository.deleteTransaction(it) } } }
    fun addCategory(category: Category) { viewModelScope.launch { repository.insertCategory(category) } }
    fun updateCategory(category: Category) { viewModelScope.launch { repository.updateCategory(category) } }
    fun deleteCategories(categories: List<Category>) { viewModelScope.launch { repository.deleteCategories(categories) } }
    
    suspend fun generateCsvString(s: Long, e: Long, n: Boolean, c: String, so: String, se: String): String = ""
}
