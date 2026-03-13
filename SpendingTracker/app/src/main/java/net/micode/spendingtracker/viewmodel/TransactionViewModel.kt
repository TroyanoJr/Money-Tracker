package net.micode.spendingtracker.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.micode.spendingtracker.model.Category
import net.micode.spendingtracker.model.Transaction
import net.micode.spendingtracker.repository.TransactionRepository

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {
    
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

    // Transacciones
    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(
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
