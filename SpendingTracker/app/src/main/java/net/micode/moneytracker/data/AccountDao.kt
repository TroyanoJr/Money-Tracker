package net.micode.moneytracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.micode.moneytracker.model.Account

/**
 * Data Access Object for the [Account] entity.
 * Provides methods for querying, inserting, updating, and deleting accounts.
 */
@Dao
interface AccountDao {
    /**
     * Retrieves all accounts from the database, ordered by their default status and name.
     * @return A Flow of a list containing all accounts.
     */
    @Query("SELECT * FROM accounts ORDER BY isDefault DESC, name ASC")
    fun getAllAccounts(): Flow<List<Account>>

    /**
     * Inserts a new account into the database. If the account already exists, it will be replaced.
     * @param account The account to insert.
     * @return The row ID of the newly inserted account.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account): Long

    /**
     * Updates an existing account in the database.
     * @param account The account with updated information.
     */
    @Update
    suspend fun updateAccount(account: Account)

    /**
     * Deletes a specific account from the database.
     * @param account The account to delete.
     */
    @Delete
    suspend fun deleteAccount(account: Account)

    /**
     * Retrieves the current default account, if one exists.
     * @return The default account or null if none is found.
     */
    @Query("SELECT * FROM accounts WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultAccount(): Account?

    /**
     * Resets the default status for all accounts to false.
     */
    @Query("UPDATE accounts SET isDefault = 0")
    suspend fun clearDefaultAccount()

    /**
     * Sets a specific account as the default account.
     * This operation is transactional and first clears any existing default account.
     * @param accountId The ID of the account to set as default.
     */
    @Transaction
    suspend fun setDefaultAccount(accountId: Long) {
        clearDefaultAccount()
        updateDefaultStatus(accountId, true)
    }

    /**
     * Updates the default status of a specific account.
     * @param accountId The ID of the account to update.
     * @param isDefault The new default status.
     */
    @Query("UPDATE accounts SET isDefault = :isDefault WHERE id = :accountId")
    suspend fun updateDefaultStatus(accountId: Long, isDefault: Boolean)
}
