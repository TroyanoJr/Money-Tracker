package net.micode.spendingtracker.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.micode.spendingtracker.model.Transaction
import net.micode.spendingtracker.repository.TransactionRepository

/**
 * ViewModel responsible for managing financial transactions.
 * Automatically listens to the TransactionRepository for new automated entries.
 */
class TransactionViewModel : ViewModel() {
    // Categories
    val expenseCategories = mutableStateListOf(
        "Eating Out" to Icons.Default.Restaurant,
        "Shopping" to Icons.Default.ShoppingCart,
        "Travel" to Icons.Default.DirectionsBus,
        "General" to Icons.Default.Sell,
        "Wifi" to Icons.Default.Wifi,
        "Water" to Icons.Default.WaterDrop,
        "School" to Icons.Default.School,
        "Clothes" to Icons.Default.Checkroom
    )
    
    val incomeCategories = mutableStateListOf(
        "Salary" to Icons.Default.Payments,
        "Bonus" to Icons.Default.Star,
        "Investment" to Icons.Default.TrendingUp
    )

    // Transactions list
    private val _transactions = mutableStateListOf<Transaction>()
    val transactions: List<Transaction> get() = _transactions

    init {
        // Automatically listen for transactions coming from the NotificationInterceptorService
        viewModelScope.launch {
            TransactionRepository.newTransactions.collect { automatedTransaction ->
                addTransaction(automatedTransaction)
            }
        }
    }

    // Computed totals
    val totalExpense: Double
        get() = _transactions.filter { it.isExpense }.sumOf { it.amount }

    val totalIncome: Double
        get() = _transactions.filter { !it.isExpense }.sumOf { it.amount }

    val balance: Double
        get() = totalIncome - totalExpense

    val expensesByCategory: List<Pair<String, Double>>
        get() = _transactions
            .filter { it.isExpense }
            .groupBy { it.categoryName }
            .map { (category, transactions) -> category to transactions.sumOf { it.amount } }

    /**
     * Adds a new transaction to the top of the list.
     */
    fun addTransaction(transaction: Transaction) {
        _transactions.add(0, transaction)
    }
}
