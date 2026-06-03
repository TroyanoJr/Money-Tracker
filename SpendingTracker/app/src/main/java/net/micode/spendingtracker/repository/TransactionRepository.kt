package net.micode.spendingtracker.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import net.micode.spendingtracker.data.AccountDao
import net.micode.spendingtracker.data.CategoryDao
import net.micode.spendingtracker.data.PeriodSummaryDao
import net.micode.spendingtracker.data.SummaryTotals
import net.micode.spendingtracker.data.TransactionDao
import net.micode.spendingtracker.model.Account
import net.micode.spendingtracker.model.Category
import net.micode.spendingtracker.model.PeriodSummary
import net.micode.spendingtracker.model.Transaction

/**
 * Repository to bridge the gap between NotificationInterceptorService, 
 * Room database, and the UI (ViewModel).
 * 
 * It manages the data flow for transactions, categories, accounts, and monthly summaries,
 * providing a unified API for the rest of the application.
 */
class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val periodSummaryDao: PeriodSummaryDao,
    private val accountDao: AccountDao
) {
    
    /**
     * Emits transactions that are newly added, typically from automated sources 
     * like the NotificationInterceptorService.
     */
    private val _newTransactions = MutableSharedFlow<Transaction>(extraBufferCapacity = 1)
    val newTransactions: SharedFlow<Transaction> = _newTransactions
    
    /**
     * Emits a signal whenever the underlying data changes, prompting the UI to refresh.
     */
    private val _dataChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val dataChanged: SharedFlow<Unit> = _dataChanged.asSharedFlow()

    // --- Transaction Methods ---

    /**
     * Returns a Flow of all transactions for a specific account.
     */
    fun getAllTransactions(accountId: Long): Flow<List<Transaction>> = transactionDao.getAllTransactions(accountId)
    
    /**
     * Retrieves transactions within a specific time range for an account.
     */
    fun getTransactionsByDateRange(accountId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(accountId, startDate, endDate)
    }

    /**
     * Provides a Pager for transaction data, allowing for efficient list rendering with 
     * support for filtering and searching.
     */
    fun getPagedTransactions(
        accountId: Long,
        startDate: Long,
        endDate: Long,
        isExpense: Boolean?,
        categoryName: String?,
        searchQuery: String? = null,
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
                    accountId = accountId,
                    startDate = startDate,
                    endDate = endDate,
                    isExpense = isExpense,
                    categoryName = categoryName,
                    searchQuery = searchQuery
                )
            }
        ).flow
    }

    /**
     * Inserts a new transaction and notifies observers.
     */
    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
        _newTransactions.emit(transaction)
        _dataChanged.emit(Unit)
    }

    /**
     * Deletes a transaction and notifies observers.
     */
    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
        _dataChanged.emit(Unit)
    }

    /**
     * Updates an existing transaction and notifies observers.
     */
    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
        _dataChanged.emit(Unit)
    }

    // --- Category Methods ---

    /**
     * Flow of all available categories.
     */
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    /**
     * Inserts a new category.
     */
    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    /**
     * Updates an existing category.
     */
    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    /**
     * Deletes a category.
     */
    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    /**
     * Batch deletes a list of categories.
     */
    suspend fun deleteCategories(categories: List<Category>) {
        categories.forEach { categoryDao.deleteCategory(it) }
    }

    // --- Period Summary & Calculation Methods ---

    /**
     * Fetches the summary configuration (like budget) for a specific period (month/year).
     */
    fun getPeriodSummary(summaryKey: String): Flow<PeriodSummary?> {
        return periodSummaryDao.getSummaryByKey(summaryKey)
    }

    /**
     * Saves or updates the summary for a specific period.
     */
    suspend fun upsertPeriodSummary(summary: PeriodSummary) {
        periodSummaryDao.upsertSummary(summary)
    }

    /**
     * Calculates the totals of income and expenses within a date range and filters.
     */
    suspend fun getSummaryTotals(
        accountId: Long,
        startDate: Long,
        endDate: Long,
        isExpense: Boolean?,
        categoryName: String?
    ): SummaryTotals {
        return transactionDao.getSummaryTotals(
            accountId = accountId,
            startDate = startDate,
            endDate = endDate,
            isExpense = isExpense,
            categoryName = categoryName
        )
    }

    /**
     * Calculates the accumulated balance (Income - Expense) before a specific date.
     * This is crucial for determining the 'Carry Over' from previous months.
     */
    suspend fun getBalanceBeforeDate(accountId: Long, beforeDate: Long): Double {
        return transactionDao.getBalanceBeforeDate(accountId, beforeDate)
    }

    /**
     * Returns the timestamp of the oldest transaction recorded for an account.
     */
    suspend fun getOldestTransactionDate(accountId: Long): Long? {
        return transactionDao.getOldestTransactionDate(accountId)
    }

    /**
     * Returns the timestamp of the first expense recorded.
     */
    suspend fun getFirstExpenseDate(accountId: Long): Long? {
        return transactionDao.getFirstExpenseDate(accountId)
    }

    /**
     * Calculates the sum of all expenses before a specific date.
     */
    suspend fun getTotalExpensesBeforeDate(accountId: Long, beforeDate: Long): Double {
        return transactionDao.getTotalExpensesBeforeDate(accountId, beforeDate)
    }

    // --- Account Methods ---

    /**
     * Flow of all registered accounts.
     */
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()

    /**
     * Adds a new account and returns its ID.
     */
    suspend fun insertAccount(account: Account): Long {
        return accountDao.insertAccount(account)
    }

    /**
     * Updates account information.
     */
    suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account)
    }

    /**
     * Deletes an account.
     */
    suspend fun deleteAccount(account: Account) {
        accountDao.deleteAccount(account)
    }

    /**
     * Sets an account as the default for the app.
     */
    suspend fun setDefaultAccount(accountId: Long) {
        accountDao.setDefaultAccount(accountId)
    }

    /**
     * Retrieves the current default account.
     */
    suspend fun getDefaultAccount(): Account? {
        return accountDao.getDefaultAccount()
    }
    
    companion object {
        @Volatile
        private var instance: TransactionRepository? = null
        
        /**
         * Singleton pattern instance provider.
         */
        fun getInstance(
            transactionDao: TransactionDao,
            categoryDao: CategoryDao,
            periodSummaryDao: PeriodSummaryDao,
            accountDao: AccountDao
        ): TransactionRepository {
            return instance ?: synchronized(this) {
                instance ?: TransactionRepository(
                    transactionDao,
                    categoryDao,
                    periodSummaryDao,
                    accountDao
                ).also { instance = it }
            }
        }
    }
}
