package net.micode.spendingtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.micode.spendingtracker.model.Account
import net.micode.spendingtracker.model.Transaction
import net.micode.spendingtracker.repository.TransactionRepository
import java.util.UUID

/**
 * ViewModel responsible for managing financial accounts.
 * Handles account creation, updates, deletion, and fund transfers between accounts.
 */
class AccountViewModel(private val repository: TransactionRepository) : ViewModel() {

    /**
     * A StateFlow containing the list of all available accounts.
     * Updates automatically when the underlying data source changes.
     */
    val allAccounts: StateFlow<List<Account>> = repository.allAccounts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Ensure at least one account exists on startup
        viewModelScope.launch {
            allAccounts.collectLatest { accounts ->
                if (accounts.isEmpty()) {
                    val existingDefault = repository.getDefaultAccount()
                    if (existingDefault == null) {
                        repository.insertAccount(
                            Account(
                                id = 1,
                                name = "Main Account",
                                color = 0xFF2196F3.toInt(), // Material Blue
                                isDefault = true
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Adds a new account to the repository.
     * @param name The display name of the account.
     * @param color The color associated with the account in the UI.
     */
    fun addAccount(name: String, color: Int) {
        viewModelScope.launch {
            val isFirst = allAccounts.value.isEmpty()
            repository.insertAccount(Account(name = name, color = color, isDefault = isFirst))
        }
    }

    /**
     * Updates an existing account's details.
     * @param account The account object containing updated information.
     */
    fun updateAccount(account: Account) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    /**
     * Deletes a specific account. 
     * Note: The system protection prevents deleting the account with ID 1.
     * @param account The account to be removed.
     */
    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            if (account.id != 1L) { // Protection for System Default account
                repository.deleteAccount(account)
            }
        }
    }

    /**
     * Deletes a list of accounts.
     * @param accounts The list of accounts to be removed.
     */
    fun deleteAccounts(accounts: List<Account>) {
        viewModelScope.launch {
            accounts.filter { it.id != 1L }.forEach { repository.deleteAccount(it) }
        }
    }

    /**
     * Sets an account as the default/selected account for the application.
     * @param accountId The unique identifier of the account to select.
     */
    fun selectAccount(accountId: Long) {
        viewModelScope.launch {
            repository.setDefaultAccount(accountId)
        }
    }

    /**
     * Transfers funds from one account to another by creating matching expense and income transactions.
     * @param fromAccountId The ID of the source account.
     * @param toAccountId The ID of the destination account.
     * @param amount The amount of money to transfer.
     * @param date The timestamp of the transfer.
     * @param note A descriptive note for the transfer.
     */
    fun transferFunds(fromAccountId: Long, toAccountId: Long, amount: Double, date: Long, note: String) {
        viewModelScope.launch {
            val transferId = UUID.randomUUID().toString()
            // Expense from source account
            repository.insertTransaction(
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
            // Income to destination account
            repository.insertTransaction(
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
    }

    /**
     * Retrieves the current default account.
     * @return The default [Account] or null if none is set.
     */
    suspend fun getDefaultAccount(): Account? {
        return repository.getDefaultAccount()
    }
}
