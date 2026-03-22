package net.micode.spendingtracker.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import net.micode.spendingtracker.data.CategoryDao
import net.micode.spendingtracker.data.PeriodSummaryDao
import net.micode.spendingtracker.data.SummaryTotals
import net.micode.spendingtracker.data.TransactionDao
import net.micode.spendingtracker.model.Category
import net.micode.spendingtracker.model.PeriodSummary
import net.micode.spendingtracker.model.Transaction

/**
 * Repository to bridge the gap between NotificationInterceptorService, 
 * Room database, and the UI (ViewModel).
 */
class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val periodSummaryDao: PeriodSummaryDao
) {
    
    // For automated transactions from NotificationInterceptorService
    private val _newTransactions = MutableSharedFlow<Transaction>(extraBufferCapacity = 1)
    val newTransactions: SharedFlow<Transaction> = _newTransactions
    private val _dataChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val dataChanged: SharedFlow<Unit> = _dataChanged.asSharedFlow()

    // Transaction methods
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }

    fun getPagedTransactions(
        startDate: Long,
        endDate: Long,
        isExpense: Boolean?,
        categoryName: String?,
        pageSize: Int = 20
    ): Flow<PagingData<Transaction>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                prefetchDistance = pageSize / 2,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                transactionDao.getPagedTransactionsByFilter(
                    startDate = startDate,
                    endDate = endDate,
                    isExpense = isExpense,
                    categoryName = categoryName
                )
            }
        ).flow
    }

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
        _newTransactions.emit(transaction)
        _dataChanged.emit(Unit)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
        _dataChanged.emit(Unit)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
        _dataChanged.emit(Unit)
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

    fun getPeriodSummary(summaryKey: String): Flow<PeriodSummary?> {
        return periodSummaryDao.getSummaryByKey(summaryKey)
    }

    suspend fun upsertPeriodSummary(summary: PeriodSummary) {
        periodSummaryDao.upsertSummary(summary)
    }

    suspend fun getSummaryTotals(
        startDate: Long,
        endDate: Long,
        isExpense: Boolean?,
        categoryName: String?
    ): SummaryTotals {
        return transactionDao.getSummaryTotals(
            startDate = startDate,
            endDate = endDate,
            isExpense = isExpense,
            categoryName = categoryName
        )
    }
    
    companion object {
        private var instance: TransactionRepository? = null
        
        fun getInstance(
            transactionDao: TransactionDao,
            categoryDao: CategoryDao,
            periodSummaryDao: PeriodSummaryDao
        ): TransactionRepository {
            return instance ?: synchronized(this) {
                instance ?: TransactionRepository(
                    transactionDao,
                    categoryDao,
                    periodSummaryDao
                ).also { instance = it }
            }
        }
    }
}
