package net.micode.moneytracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import net.micode.moneytracker.model.Category
import net.micode.moneytracker.model.Transaction
import net.micode.moneytracker.repository.TransactionRepository
import net.micode.moneytracker.util.DateManager
import net.micode.moneytracker.util.Period
import net.micode.moneytracker.util.SettingsManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel responsible for data analysis and generating financial reports.
 * It provides streams for heatmap data, cash flow charts, and category distribution.
 */
class ReportsViewModel(
    private val repository: TransactionRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(
        try { Period.valueOf(settingsManager.getDefaultTimePeriod()) } catch (e: Exception) { Period.MONTH }
    )
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _selectedAccountId = MutableStateFlow<Long>(1L)
    val selectedAccountId: StateFlow<Long> = _selectedAccountId.asStateFlow()

    private val _reportIsExpense = MutableStateFlow(true)
    val reportIsExpense: StateFlow<Boolean> = _reportIsExpense.asStateFlow()

    private val dateRange = combine(_selectedPeriod, _selectedDate) { p, d ->
        DateManager.calculateDateRange(p, d, settingsManager.getMonthStartDay(), settingsManager.getWeekStartDay())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DateManager.calculateDateRange(
        try { Period.valueOf(settingsManager.getDefaultTimePeriod()) } catch (e: Exception) { Period.MONTH },
        System.currentTimeMillis(),
        settingsManager.getMonthStartDay(),
        settingsManager.getWeekStartDay()
    ))

    /**
     * Observable list of all categories from the repository.
     */
    private val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Flow that provides transactions filtered by account and date range.
     */
    private val transactionsInPeriod: StateFlow<List<Transaction>> = combine(
        dateRange, 
        _selectedAccountId, 
        repository.dataChanged.onStart { emit(Unit) }
    ) { range, id, _ ->
        range to id
    }.flatMapLatest { (range, id) -> 
        repository.getTransactionsByDateRange(id, range.start, range.end) 
    }.flowOn(Dispatchers.Default)
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Calculated data for category-based reports (e.g., Pie charts).
     */
    val categoryReportData: StateFlow<List<CategoryReportItem>> = combine(
        transactionsInPeriod,
        categories,
        _reportIsExpense
    ) { txs, cats, isExp ->
        val filtered = txs.filter { it.isExpense == isExp }
        val total = filtered.sumOf { it.amount }
        filtered.groupBy { it.categoryName }.map { (n, ts) ->
            val amt = ts.sumOf { it.amount }
            val c = cats.find { it.name == n }
            CategoryReportItem(n, amt, c?.color ?: -0x1, if (total > 0) (amt / total).toFloat() else 0f)
        }.sortedByDescending { it.amount }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Data formatted for time-based cash flow charts.
     */
    val cashFlowData: StateFlow<List<CashFlowPoint>> = combine(
        transactionsInPeriod, 
        _selectedPeriod, 
        _selectedDate
    ) { list, period, date ->
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
                    cal.set(Calendar.HOUR_OF_DAY, h)
                    points.add(CashFlowPoint(sdf.format(cal.time), ts.filter { !it.isExpense }.sumOf { it.amount }, ts.filter { it.isExpense }.sumOf { it.amount }, cal.timeInMillis)) 
                }
            }
            Period.WEEK -> {
                val sdf = SimpleDateFormat("EEE", Locale.getDefault())
                val weekStartDay = settingsManager.getWeekStartDay()
                
                val temp = cal.clone() as Calendar
                // Adjust temp to the start of the week based on weekStartDay
                var diff = temp.get(Calendar.DAY_OF_WEEK) - weekStartDay
                if (diff < 0) diff += 7
                temp.add(Calendar.DAY_OF_YEAR, -diff)

                for (i in 0..6) { 
                    val day = temp.get(Calendar.DAY_OF_WEEK)
                    val ts = grouped[day] ?: emptyList()
                    points.add(CashFlowPoint(sdf.format(temp.time), ts.filter { !it.isExpense }.sumOf { it.amount }, ts.filter { it.isExpense }.sumOf { it.amount }, temp.timeInMillis))
                    temp.add(Calendar.DAY_OF_MONTH, 1) 
                }
            }
            Period.MONTH -> {
                val sdf = SimpleDateFormat("d", Locale.getDefault())
                val startDay = settingsManager.getMonthStartDay()
                
                val temp = cal.clone() as Calendar
                if (temp.get(Calendar.DAY_OF_MONTH) < startDay) {
                    temp.add(Calendar.MONTH, -1)
                }
                temp.set(Calendar.DAY_OF_MONTH, startDay.coerceAtMost(temp.getActualMaximum(Calendar.DAY_OF_MONTH)))
                
                val endCal = temp.clone() as Calendar
                endCal.add(Calendar.MONTH, 1)
                
                while (temp.before(endCal)) {
                    val d = temp.get(Calendar.DAY_OF_MONTH)
                    val ts = grouped[d] ?: emptyList()
                    points.add(CashFlowPoint(sdf.format(temp.time), ts.filter { !it.isExpense }.sumOf { it.amount }, ts.filter { it.isExpense }.sumOf { it.amount }, temp.timeInMillis))
                    temp.add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            Period.YEAR -> {
                val sdf = SimpleDateFormat("MMM", Locale.getDefault())
                for (m in 0..11) { 
                    val ts = grouped[m] ?: emptyList()
                    cal.set(Calendar.MONTH, m)
                    points.add(CashFlowPoint(sdf.format(cal.time), ts.filter { !it.isExpense }.sumOf { it.amount }, ts.filter { it.isExpense }.sumOf { it.amount }, cal.timeInMillis)) 
                }
            }
        }
        points
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Map of daily financial activity for heatmap visualization.
     */
    val heatmapData: StateFlow<Map<Long, Double>> = combine(dateRange, transactionsInPeriod) { range, list ->
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
            cal.timeInMillis = tx.date; cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            val day = cal.timeInMillis
            if (map.containsKey(day)) map[day] = (map[day] ?: 0.0) + (if (tx.isExpense) -tx.amount else tx.amount)
        }
        map
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setPeriod(period: Period) { _selectedPeriod.value = period }
    fun setDate(dateMillis: Long) { _selectedDate.value = dateMillis }
    fun setAccountId(id: Long) { _selectedAccountId.value = id }
    fun setReportIsExpense(isExpense: Boolean) { _reportIsExpense.value = isExpense }
}
