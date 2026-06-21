package net.micode.moneytracker.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import net.micode.moneytracker.data.AccountDao
import net.micode.moneytracker.data.CategoryDao
import net.micode.moneytracker.data.PeriodSummaryDao
import net.micode.moneytracker.data.SummaryTotals
import net.micode.moneytracker.data.TransactionDao
import net.micode.moneytracker.model.Account
import net.micode.moneytracker.model.Category
import net.micode.moneytracker.model.PeriodSummary
import net.micode.moneytracker.model.Transaction
import java.util.UUID

/**
 * Repository class that abstracts access to multiple data sources.
 * It provides a clean API for the UI to interact with transaction, category, and account data.
 */
class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val periodSummaryDao: PeriodSummaryDao,
    private val accountDao: AccountDao
) {
    
    /**
     * SharedFlow to notify observers about new transactions, typically from background services.
     */
    private val _newTransactions = MutableSharedFlow<Transaction>(extraBufferCapacity = 1)
    val newTransactions: SharedFlow<Transaction> = _newTransactions
    
    /**
     * SharedFlow to notify observers whenever any transaction-related data changes in the database.
     */
    private val _dataChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val dataChanged: SharedFlow<Unit> = _dataChanged.asSharedFlow()

    // --- Transaction Methods ---

    /**
     * Returns a Flow of all transactions associated with a specific account.
     */
    fun getAllTransactions(accountId: Long): Flow<List<Transaction>> = transactionDao.getAllTransactions(accountId)
    
    /**
     * Returns a Flow of transactions within a specific date range for an account.
     */
    fun getTransactionsByDateRange(accountId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(accountId, startDate, endDate)
    }

    /**
     * Provides a paged Flow of transactions based on various filters.
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
     * Deletes a list of transactions and notifies observers once.
     */
    suspend fun deleteTransactions(transactions: List<Transaction>) {
        transactions.forEach { transactionDao.deleteTransaction(it) }
        _dataChanged.emit(Unit)
    }

    /**
     * Updates an existing transaction and notifies observers.
     */
    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
        _dataChanged.emit(Unit)
    }

    /**
     * Executes a fund transfer between accounts by creating two complementary transactions.
     * This ensures atomicity and consistency in the transfer logic across the app.
     */
    suspend fun transferFunds(fromAccountId: Long, toAccountId: Long, amount: Double, date: Long, note: String) {
        val transferId = UUID.randomUUID().toString()
        // Outgoing transaction from source account
        insertTransaction(
            Transaction(
                id = "${transferId}_out",
                amount = amount,
                categoryName = "Transfer",
                date = date,
                note = note,
                isExpense = true,
                accountId = fromAccountId
            )
        )
        // Incoming transaction to destination account
        insertTransaction(
            Transaction(
                id = "${transferId}_in",
                amount = amount,
                categoryName = "Transfer",
                date = date,
                note = note,
                isExpense = false,
                accountId = toAccountId
            )
        )
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
     * Deletes a list of categories.
     */
    suspend fun deleteCategories(categories: List<Category>) {
        categories.forEach { categoryDao.deleteCategory(it) }
    }

    /**
     * Seeds the database with a set of default categories.
     */
    suspend fun seedDefaultCategories() {
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
        defaults.forEach { categoryDao.insertCategory(it) }
    }

    /**
     * Ensures that the mandatory "Transfer" category exists in the system.
     */
    suspend fun ensureTransferCategoryExists() {
        insertCategory(Category(name = "Transfer", iconName = "SyncAlt", isExpense = true, color = -0x1000000))
    }

    // --- Summary and Analytic Methods ---

    /**
     * Retrieves a period summary by its unique key.
     */
    fun getPeriodSummary(summaryKey: String): Flow<PeriodSummary?> {
        return periodSummaryDao.getSummaryByKey(summaryKey)
    }

    /**
     * Inserts or updates a period summary record.
     */
    suspend fun upsertPeriodSummary(summary: PeriodSummary) {
        periodSummaryDao.upsertSummary(summary)
    }

    /**
     * Calculates totals (income/expense) for a filtered set of transactions.
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
     * Calculates the net balance of an account before a specific date.
     */
    suspend fun getBalanceBeforeDate(accountId: Long, beforeDate: Long): Double {
        return transactionDao.getBalanceBeforeDate(accountId, beforeDate)
    }

    /**
     * Retrieves the date of the oldest transaction for an account.
     */
    suspend fun getOldestTransactionDate(accountId: Long): Long? {
        return transactionDao.getOldestTransactionDate(accountId)
    }

    /**
     * Retrieves the date of the first expense for an account.
     */
    suspend fun getFirstExpenseDate(accountId: Long): Long? {
        return transactionDao.getFirstExpenseDate(accountId)
    }

    /**
     * Sums all expenses for an account prior to a given date.
     */
    suspend fun getTotalExpensesBeforeDate(accountId: Long, beforeDate: Long): Double {
        return transactionDao.getTotalExpensesBeforeDate(accountId, beforeDate)
    }

    /**
     * Sums all income for an account prior to a given date.
     */
    suspend fun getTotalIncomeBeforeDate(accountId: Long, beforeDate: Long): Double {
        return transactionDao.getTotalIncomeBeforeDate(accountId, beforeDate)
    }

    // --- Account Methods ---

    /**
     * Flow of all available financial accounts.
     */
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()

    /**
     * Inserts a new account and returns its generated ID.
     */
    suspend fun insertAccount(account: Account): Long {
        return accountDao.insertAccount(account)
    }

    /**
     * Updates an account's details.
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
     * Designates a specific account as the default.
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
         * Singleton pattern to provide a thread-safe instance of the repository.
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
