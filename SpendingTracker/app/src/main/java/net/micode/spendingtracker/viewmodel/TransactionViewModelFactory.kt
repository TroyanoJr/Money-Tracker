package net.micode.spendingtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.micode.spendingtracker.repository.TransactionRepository
import net.micode.spendingtracker.util.SettingsManager

/**
 * Factory class for creating instances of ViewModels in the application.
 * This factory handles the injection of the [TransactionRepository] and [SettingsManager] dependencies.
 */
class TransactionViewModelFactory(
    private val repository: TransactionRepository,
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given [modelClass].
     * Supported ViewModels: [TransactionViewModel], [AccountViewModel].
     * 
     * @param modelClass The class of the ViewModel to create.
     * @return A newly created ViewModel instance of type [T].
     * @throws IllegalArgumentException if the provided [modelClass] is not supported.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(TransactionViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                TransactionViewModel(repository, settingsManager) as T
            }
            modelClass.isAssignableFrom(AccountViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AccountViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
