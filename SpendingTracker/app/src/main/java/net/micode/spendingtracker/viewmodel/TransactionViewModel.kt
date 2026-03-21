package net.micode.spendingtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.micode.spendingtracker.model.Category
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

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {
    
    // UI States for Filtering
    private val _selectedPeriod = MutableStateFlow(Period.MONTH)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    // Currency State
    private val _currencySymbol = MutableStateFlow(settingsManager.getCurrencySymbol())
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    // Transaction Filtering States
    private val _activeFilterType = MutableStateFlow(FilterType.ALL)
    val activeFilterType: StateFlow<FilterType> = _activeFilterType.asStateFlow()

    private val _filterCategoryName = MutableStateFlow<String?>(null)
    val filterCategoryName: StateFlow<String?> = _filterCategoryName.asStateFlow()

    // Categorías desde la base de datos
    val categories: StateFlow<List<Category>> = repository.allCategories
        .onEach { list ->
            if (list.isEmpty() && !settingsManager.hasSeededCategories()) {
                seedDefaultCategories()
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

    // Transacciones filtradas por periodo Y por tipo/categoría
    val transactions: StateFlow<List<Transaction>> = combine(
        repository.allTransactions,
        _selectedPeriod,
        _selectedDate,
        _activeFilterType,
        _filterCategoryName
    ) { allTransactions, period, date, filterType, catName ->
        val timeFiltered = filterTransactionsByPeriod(allTransactions, period, date)
        
        when (filterType) {
            FilterType.ALL -> timeFiltered
            FilterType.ONLY_EXPENSE -> timeFiltered.filter { it.isExpense }
            FilterType.ONLY_INCOME -> timeFiltered.filter { !it.isExpense }
            FilterType.BY_CATEGORY -> if (catName != null) {
                timeFiltered.filter { it.categoryName == catName }
            } else timeFiltered
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Reporte de categorías con colores y porcentajes
    val categoryReportData: StateFlow<List<CategoryReportItem>> = combine(
        transactions,
        categories
    ) { currentTransactions, allCategories ->
        val expenses = currentTransactions.filter { it.isExpense }
        val totalAmount = expenses.sumOf { it.amount }
        
        expenses.groupBy { it.categoryName }
            .map { (name, transList) ->
                val amount = transList.sumOf { it.amount }
                val category = allCategories.find { it.name == name }
                CategoryReportItem(
                    name = name,
                    amount = amount,
                    color = category?.color ?: -0x616162,
                    percentage = if (totalAmount > 0) (amount / totalAmount).toFloat() else 0f
                )
            }
            .sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cashFlowData: StateFlow<List<CashFlowPoint>> = combine(
        repository.allTransactions,
        _selectedPeriod,
        _selectedDate
    ) { allTransactions, period, date ->
        calculateCashFlow(allTransactions, period, date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Methods for Filtering
    fun setFilter(type: FilterType, categoryName: String? = null) {
        _activeFilterType.value = type
        _filterCategoryName.value = categoryName
    }

    fun refreshCurrency() {
        _currencySymbol.value = settingsManager.getCurrencySymbol()
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

    private fun calculateCashFlow(allTransactions: List<Transaction>, period: Period, dateMillis: Long): List<CashFlowPoint> {
        val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val points = mutableListOf<CashFlowPoint>()
        
        when (period) {
            Period.YEAR -> {
                val year = calendar.get(Calendar.YEAR)
                val df = SimpleDateFormat("MMM", Locale.getDefault())
                for (month in 0..11) {
                    calendar.set(year, month, 1)
                    val startTime = calendar.timeInMillis
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                    val endTime = calendar.timeInMillis
                    val monthTrans = allTransactions.filter { it.date in startTime..endTime }
                    points.add(CashFlowPoint(
                        label = df.format(calendar.time),
                        income = monthTrans.filter { !it.isExpense }.sumOf { it.amount },
                        expense = monthTrans.filter { it.isExpense }.sumOf { it.amount },
                        timestamp = startTime
                    ))
                }
            }
            Period.MONTH -> {
                val month = calendar.get(Calendar.MONTH)
                val year = calendar.get(Calendar.YEAR)
                calendar.set(year, month, 1)
                var weekNum = 1
                while (calendar.get(Calendar.MONTH) == month) {
                    val startTime = calendar.timeInMillis
                    calendar.add(Calendar.DAY_OF_MONTH, 6)
                    if (calendar.get(Calendar.MONTH) != month) calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                    val endTime = calendar.timeInMillis
                    val weekTrans = allTransactions.filter { it.date in startTime..endTime }
                    points.add(CashFlowPoint(label = "W$weekNum", income = weekTrans.filter { !it.isExpense }.sumOf { it.amount }, expense = weekTrans.filter { it.isExpense }.sumOf { it.amount }, timestamp = startTime))
                    calendar.add(Calendar.DAY_OF_MONTH, 1); weekNum++
                }
            }
            else -> {
                val df = SimpleDateFormat("dd/MM", Locale.getDefault())
                calendar.add(Calendar.DAY_OF_YEAR, -6)
                for (i in 0..6) {
                    val startTime = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
                    val endTime = calendar.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                    val dayTrans = allTransactions.filter { it.date in startTime..endTime }
                    points.add(CashFlowPoint(label = df.format(calendar.time), income = dayTrans.filter { !it.isExpense }.sumOf { it.amount }, expense = dayTrans.filter { it.isExpense }.sumOf { it.amount }, timestamp = startTime))
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }
        return points
    }

    // Export CSV logic
    suspend fun generateCsvString(
        startDate: Long,
        endDate: Long,
        useNegativeForExpense: Boolean,
        categoryName: String,
        sortBy: String,
        separatorName: String
    ): String {
        val allTransactions = repository.allTransactions.first()
        
        // Normalize dates to start and end of day
        val startCal = Calendar.getInstance().apply { timeInMillis = startDate; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }
        val endCal = Calendar.getInstance().apply { timeInMillis = endDate; set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }
        
        var filtered = allTransactions.filter { it.date in startCal.timeInMillis..endCal.timeInMillis }
        
        if (categoryName != "All Categories") {
            filtered = filtered.filter { it.categoryName == categoryName }
        }
        
        filtered = when (sortBy) {
            "Date (Newest First)" -> filtered.sortedByDescending { it.date }
            "Date (Oldest First)" -> filtered.sortedBy { it.date }
            "Amount (Highest First)" -> filtered.sortedByDescending { it.amount }
            "Amount (Lowest First)" -> filtered.sortedBy { it.amount }
            else -> filtered.sortedByDescending { it.date }
        }
        
        val separator = when (separatorName) {
            "Comma" -> ","
            "Semicolon" -> ";"
            "Tab" -> "\t"
            else -> ","
        }
        
        val sb = StringBuilder()
        sb.append(listOf("Date", "Type", "Category", "Amount").joinToString(separator)).append("\n")
        
        val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        filtered.forEach { trans ->
            val amount = if (trans.isExpense && useNegativeForExpense) -trans.amount else trans.amount
            val type = if (trans.isExpense) "Expense" else "Income"
            val row = listOf(
                df.format(Date(trans.date)),
                type,
                trans.categoryName,
                String.format(Locale.US, "%.2f", amount)
            )
            sb.append(row.joinToString(separator)).append("\n")
        }
        
        return sb.toString()
    }

    // Heatmap data
    val heatmapData: StateFlow<Map<Long, Double>> = combine(
        repository.allTransactions,
        _selectedPeriod,
        _selectedDate
    ) { allTransactions, period, date ->
        calculateHeatmapData(allTransactions, period, date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    private fun calculateHeatmapData(transactions: List<Transaction>, period: Period, dateMillis: Long): Map<Long, Double> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val startCalendar = calendar.clone() as Calendar
        val endCalendar = calendar.clone() as Calendar
        when (period) {
            Period.WEEK -> { startCalendar.set(Calendar.DAY_OF_WEEK, startCalendar.firstDayOfWeek); endCalendar.set(Calendar.DAY_OF_WEEK, startCalendar.firstDayOfWeek); endCalendar.add(Calendar.DAY_OF_WEEK, 6) }
            Period.MONTH -> { startCalendar.set(Calendar.DAY_OF_MONTH, 1); endCalendar.set(Calendar.DAY_OF_MONTH, startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)) }
            Period.YEAR -> { startCalendar.set(Calendar.MONTH, Calendar.JANUARY); startCalendar.set(Calendar.DAY_OF_MONTH, 1); endCalendar.set(Calendar.MONTH, Calendar.DECEMBER); endCalendar.set(Calendar.DAY_OF_MONTH, 31) }
            else -> {}
        }
        val result = mutableMapOf<Long, Double>()
        val current = startCalendar.clone() as Calendar
        while (!current.after(endCalendar)) {
            result[current.timeInMillis] = 0.0
            current.add(Calendar.DAY_OF_YEAR, 1)
        }
        transactions.forEach { trans ->
            val transCal = Calendar.getInstance().apply { timeInMillis = trans.date; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
            val time = transCal.timeInMillis
            if (result.containsKey(time)) {
                val amount = if (trans.isExpense) -trans.amount else trans.amount
                result[time] = result[time]!! + amount
            }
        }
        return result
    }

    fun setPeriod(period: Period) { _selectedPeriod.value = period }
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

    private fun filterTransactionsByPeriod(transactions: List<Transaction>, period: Period, dateMillis: Long): List<Transaction> {
        val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val targetYear = calendar.get(Calendar.YEAR)
        val targetMonth = calendar.get(Calendar.MONTH)
        val targetDay = calendar.get(Calendar.DAY_OF_YEAR)
        val targetWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        return transactions.filter {
            val transCal = Calendar.getInstance().apply { timeInMillis = it.date }
            when (period) {
                Period.DAY -> transCal.get(Calendar.YEAR) == targetYear && transCal.get(Calendar.DAY_OF_YEAR) == targetDay
                Period.WEEK -> transCal.get(Calendar.YEAR) == targetYear && transCal.get(Calendar.WEEK_OF_YEAR) == targetWeek
                Period.MONTH -> transCal.get(Calendar.YEAR) == targetYear && transCal.get(Calendar.MONTH) == targetMonth
                Period.YEAR -> transCal.get(Calendar.YEAR) == targetYear
            }
        }
    }

    val incompleteTransactionsCount: StateFlow<Int> = repository.allTransactions
        .map { list -> list.count { !it.isComplete } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalExpense: StateFlow<Double> = transactions
        .map { list -> list.filter { it.isExpense }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIncome: StateFlow<Double> = transactions
        .map { list -> list.filter { !it.isExpense }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val balance: StateFlow<Double> = transactions
        .map { list -> 
            val income = list.filter { !it.isExpense }.sumOf { it.amount }
            val expense = list.filter { it.isExpense }.sumOf { it.amount }
            income - expense
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val expensesByCategory: StateFlow<List<Pair<String, Double>>> = transactions
        .map { list ->
            list.filter { it.isExpense }
                .groupBy { it.categoryName }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
                .toList()
                .sortedByDescending { it.second }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTransaction(transaction: Transaction) { viewModelScope.launch { repository.insertTransaction(transaction) } }
    fun updateTransaction(transaction: Transaction) { viewModelScope.launch { repository.updateTransaction(transaction) } }
    fun deleteTransaction(transaction: Transaction) { viewModelScope.launch { repository.deleteTransaction(transaction) } }
    fun deleteTransactions(transactions: List<Transaction>) { viewModelScope.launch { transactions.forEach { repository.deleteTransaction(it) } } }
    fun addCategory(category: Category) { viewModelScope.launch { repository.insertCategory(category) } }
    fun updateCategory(category: Category) { viewModelScope.launch { repository.updateCategory(category) } }
    fun deleteCategories(categories: List<Category>) { viewModelScope.launch { repository.deleteCategories(categories) } }
}
