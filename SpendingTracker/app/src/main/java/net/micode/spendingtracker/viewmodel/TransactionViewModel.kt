package net.micode.spendingtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.micode.spendingtracker.model.Category
import net.micode.spendingtracker.model.Transaction
import net.micode.spendingtracker.repository.TransactionRepository
import java.util.*

enum class Period {
    DAY, WEEK, MONTH, YEAR
}

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {
    
    // UI States for Filtering
    private val _selectedPeriod = MutableStateFlow(Period.MONTH)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    // Categorías desde la base de datos
    val categories: StateFlow<List<Category>> = repository.allCategories
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

    // Transacciones filtradas por periodo
    val transactions: StateFlow<List<Transaction>> = combine(
        repository.allTransactions,
        _selectedPeriod,
        _selectedDate
    ) { allTransactions, period, date ->
        filterTransactionsByPeriod(allTransactions, period, date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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

    // Heatmap data: Map of timestamp to balance
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
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val startCalendar = calendar.clone() as Calendar
        val endCalendar = calendar.clone() as Calendar
        
        when (period) {
            Period.DAY -> {
                // Just one day
            }
            Period.WEEK -> {
                startCalendar.set(Calendar.DAY_OF_WEEK, startCalendar.firstDayOfWeek)
                endCalendar.set(Calendar.DAY_OF_WEEK, startCalendar.firstDayOfWeek)
                endCalendar.add(Calendar.DAY_OF_WEEK, 6)
            }
            Period.MONTH -> {
                startCalendar.set(Calendar.DAY_OF_MONTH, 1)
                endCalendar.set(Calendar.DAY_OF_MONTH, startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            }
            Period.YEAR -> {
                startCalendar.set(Calendar.MONTH, Calendar.JANUARY)
                startCalendar.set(Calendar.DAY_OF_MONTH, 1)
                endCalendar.set(Calendar.MONTH, Calendar.DECEMBER)
                endCalendar.set(Calendar.DAY_OF_MONTH, 31)
            }
        }
        
        val result = mutableMapOf<Long, Double>()
        val current = startCalendar.clone() as Calendar
        while (!current.after(endCalendar)) {
            result[current.timeInMillis] = 0.0
            current.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        transactions.forEach { trans ->
            val transCal = Calendar.getInstance().apply {
                timeInMillis = trans.date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val time = transCal.timeInMillis
            if (result.containsKey(time)) {
                val amount = if (trans.isExpense) -trans.amount else trans.amount
                result[time] = result[time]!! + amount
            }
        }
        
        return result
    }

    // Methods for Filtering
    fun setPeriod(period: Period) {
        _selectedPeriod.value = period
    }

    fun nextPeriod() {
        _selectedDate.value = adjustDate(_selectedDate.value, _selectedPeriod.value, 1)
    }

    fun previousPeriod() {
        _selectedDate.value = adjustDate(_selectedDate.value, _selectedPeriod.value, -1)
    }

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

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun deleteTransactions(transactions: List<Transaction>) {
        viewModelScope.launch {
            transactions.forEach { repository.deleteTransaction(it) }
        }
    }

    // Métodos para categorías
    fun addCategory(category: Category) {
        viewModelScope.launch {
            repository.insertCategory(category)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategories(categories: List<Category>) {
        viewModelScope.launch {
            repository.deleteCategories(categories)
        }
    }
}
