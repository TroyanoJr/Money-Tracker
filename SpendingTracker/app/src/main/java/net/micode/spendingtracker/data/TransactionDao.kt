package net.micode.spendingtracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.micode.spendingtracker.model.Transaction

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Delete
    suspend fun deleteTransaction(transaction: Transaction): Int

    @Update
    suspend fun updateTransaction(transaction: Transaction): Int
}
