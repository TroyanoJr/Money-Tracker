package net.micode.spendingtracker.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import net.micode.spendingtracker.model.Transaction

/**
 * Singleton repository to bridge the gap between NotificationInterceptorService
 * and the UI (ViewModel).
 */
object TransactionRepository {
    private val _newTransactions = MutableSharedFlow<Transaction>(extraBufferCapacity = 1)
    val newTransactions: SharedFlow<Transaction> = _newTransactions

    suspend fun addTransaction(transaction: Transaction) {
        _newTransactions.emit(transaction)
    }
}
