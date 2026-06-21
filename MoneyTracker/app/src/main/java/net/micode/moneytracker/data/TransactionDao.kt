package net.micode.moneytracker.data

import androidx.room.*
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import net.micode.moneytracker.model.Transaction

/**
 * Data Access Object for the [Transaction] entity.
 * Provides a comprehensive set of methods for managing and analyzing financial transactions.
 */
@Dao
interface TransactionDao {
    /**
     * Retrieves all transactions for a specific account, or all accounts if accountId is -1.
     * @param accountId The ID of the account to filter by (-1 for all).
     * @return A Flow of a list of transactions ordered by date descending.
     */
    @Query("SELECT * FROM transactions WHERE (:accountId = -1 OR accountId = :accountId) ORDER BY date DESC")
    fun getAllTransactions(accountId: Long): Flow<List<Transaction>>

    /**
     * Retrieves transactions within a specific date range for an account.
     * @param accountId The ID of the account (-1 for all).
     * @param startDate The starting timestamp.
     * @param endDate The ending timestamp.
     * @return A Flow of filtered transactions.
     */
    @Query("SELECT * FROM transactions WHERE (:accountId = -1 OR accountId = :accountId) AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(accountId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>>

    /**
     * Provides a [PagingSource] for paginated transaction lists with multiple filters.
     * @param accountId Account filter ID.
     * @param startDate Start date of the range.
     * @param endDate End date of the range.
     * @param isExpense Filter by expense (true), income (false), or both (null).
     * @param categoryName Filter by a specific category name.
     * @param searchQuery Search term for the category name.
     */
    @Query(
        """
        SELECT * FROM transactions
        WHERE (:accountId = -1 OR accountId = :accountId)
          AND date BETWEEN :startDate AND :endDate
          AND (:isExpense IS NULL OR isExpense = :isExpense)
          AND (:categoryName IS NULL OR categoryName = :categoryName)
          AND (:searchQuery IS NULL OR categoryName LIKE '%' || :searchQuery || '%')
        ORDER BY date DESC
        """
    )
    fun getPagedTransactionsByFilter(
        accountId: Long,
        startDate: Long,
        endDate: Long,
        isExpense: Boolean?,
        categoryName: String?,
        searchQuery: String? = null
    ): PagingSource<Int, Transaction>

    /**
     * Calculates the total income and expense for a given period and set of filters.
     */
    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN isExpense = 0 THEN amount ELSE 0 END), 0) AS totalIncome,
            COALESCE(SUM(CASE WHEN isExpense = 1 THEN amount ELSE 0 END), 0) AS totalExpense
        FROM transactions
        WHERE (:accountId = -1 OR accountId = :accountId)
          AND date BETWEEN :startDate AND :endDate
          AND (:isExpense IS NULL OR isExpense = :isExpense)
          AND (:categoryName IS NULL OR categoryName = :categoryName)
        """
    )
    suspend fun getSummaryTotals(
        accountId: Long,
        startDate: Long,
        endDate: Long,
        isExpense: Boolean?,
        categoryName: String?
    ): SummaryTotals

    /**
     * Calculates the cumulative balance (Income - Expense) before a specific date.
     */
    @Query(
        """
        SELECT 
            COALESCE(SUM(CASE WHEN isExpense = 0 THEN amount ELSE -amount END), 0)
        FROM transactions
        WHERE (:accountId = -1 OR accountId = :accountId)
          AND date < :beforeDate
        """
    )
    suspend fun getBalanceBeforeDate(accountId: Long, beforeDate: Long): Double

    /**
     * Gets the timestamp of the very first transaction recorded.
     */
    @Query("SELECT MIN(date) FROM transactions WHERE (:accountId = -1 OR accountId = :accountId)")
    suspend fun getOldestTransactionDate(accountId: Long): Long?

    /**
     * Gets the timestamp of the first expense recorded.
     */
    @Query("SELECT MIN(date) FROM transactions WHERE (:accountId = -1 OR accountId = :accountId) AND isExpense = 1 AND amount > 0")
    suspend fun getFirstExpenseDate(accountId: Long): Long?

    /**
     * Sums all expenses recorded before a specific date.
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE (:accountId = -1 OR accountId = :accountId) AND isExpense = 1 AND date < :beforeDate")
    suspend fun getTotalExpensesBeforeDate(accountId: Long, beforeDate: Long): Double

    /**
     * Sums all income recorded before a specific date.
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE (:accountId = -1 OR accountId = :accountId) AND isExpense = 0 AND date < :beforeDate")
    suspend fun getTotalIncomeBeforeDate(accountId: Long, beforeDate: Long): Double

    /**
     * Inserts a new transaction into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    /**
     * Deletes a transaction from the database.
     */
    @Delete
    suspend fun deleteTransaction(transaction: Transaction): Int

    /**
     * Updates an existing transaction.
     */
    @Update
    suspend fun updateTransaction(transaction: Transaction): Int
}
