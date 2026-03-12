package net.micode.spendingtracker.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import net.micode.spendingtracker.data.CategoryDao
import net.micode.spendingtracker.data.TransactionDao
import net.micode.spendingtracker.model.Category
import net.micode.spendingtracker.model.Transaction

/**
 * Repository to bridge the gap between NotificationInterceptorService, 
 * Room database, and the UI (ViewModel).
 */
class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {
    
    // For automated transactions from NotificationInterceptorService
    private val _newTransactions = MutableSharedFlow<Transaction>(extraBufferCapacity = 1)
    val newTransactions: SharedFlow<Transaction> = _newTransactions

    // Transaction methods
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
        _newTransactions.emit(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    // Category methods
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    suspend fun deleteCategories(categories: List<Category>) {
        categories.forEach { categoryDao.deleteCategory(it) }
    }
    
    companion object {
        private var instance: TransactionRepository? = null
        
        fun getInstance(transactionDao: TransactionDao, categoryDao: CategoryDao): TransactionRepository {
            return instance ?: synchronized(this) {
                instance ?: TransactionRepository(transactionDao, categoryDao).also { instance = it }
            }
        }
    }
}
