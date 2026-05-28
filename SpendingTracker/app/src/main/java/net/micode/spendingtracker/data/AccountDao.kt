package net.micode.spendingtracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.micode.spendingtracker.model.Account

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY isDefault DESC, name ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account): Long // Ahora devuelve el ID generado

    @Update
    suspend fun updateAccount(account: Account)

    @Delete
    suspend fun deleteAccount(account: Account)

    @Query("SELECT * FROM accounts WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultAccount(): Account?

    @Query("UPDATE accounts SET isDefault = 0")
    suspend fun clearDefaultAccount()

    @Transaction
    suspend fun setDefaultAccount(accountId: Long) {
        clearDefaultAccount()
        updateDefaultStatus(accountId, true)
    }

    @Query("UPDATE accounts SET isDefault = :isDefault WHERE id = :accountId")
    suspend fun updateDefaultStatus(accountId: Long, isDefault: Boolean)
}
