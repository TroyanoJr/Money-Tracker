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

class AccountViewModel(private val repository: TransactionRepository) : ViewModel() {

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

    fun addAccount(name: String, color: Int) {
        viewModelScope.launch {
            val isFirst = allAccounts.value.isEmpty()
            repository.insertAccount(Account(name = name, color = color, isDefault = isFirst))
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            if (account.id != 1L) { // Protection for System Default account
                repository.deleteAccount(account)
            }
        }
    }

    fun deleteAccounts(accounts: List<Account>) {
        viewModelScope.launch {
            accounts.filter { it.id != 1L }.forEach { repository.deleteAccount(it) }
        }
    }

    fun selectAccount(accountId: Long) {
        viewModelScope.launch {
            repository.setDefaultAccount(accountId)
        }
    }

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

    suspend fun getDefaultAccount(): Account? {
        return repository.getDefaultAccount()
    }
}
