package net.micode.spendingtracker.data

import androidx.room.*
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import net.micode.spendingtracker.model.Transaction

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE date BETWEEN :startDate AND :endDate
          AND (:isExpense IS NULL OR isExpense = :isExpense)
          AND (:categoryName IS NULL OR categoryName = :categoryName)
        ORDER BY date DESC
        """
    )
    fun getPagedTransactionsByFilter(
        startDate: Long,
        endDate: Long,
        isExpense: Boolean?,
        categoryName: String?
    ): PagingSource<Int, Transaction>

    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN isExpense = 0 THEN amount ELSE 0 END), 0) AS totalIncome,
            COALESCE(SUM(CASE WHEN isExpense = 1 THEN amount ELSE 0 END), 0) AS totalExpense
        FROM transactions
        WHERE date BETWEEN :startDate AND :endDate
          AND (:isExpense IS NULL OR isExpense = :isExpense)
          AND (:categoryName IS NULL OR categoryName = :categoryName)
        """
    )
    suspend fun getSummaryTotals(
        startDate: Long,
        endDate: Long,
        isExpense: Boolean?,
        categoryName: String?
    ): SummaryTotals

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Delete
    suspend fun deleteTransaction(transaction: Transaction): Int

    @Update
    suspend fun updateTransaction(transaction: Transaction): Int
}
