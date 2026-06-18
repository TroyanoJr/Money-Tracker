package net.micode.moneytracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import net.micode.moneytracker.ui.screens.DashboardScreen
import net.micode.moneytracker.ui.screens.LockScreen
import net.micode.moneytracker.viewmodel.TransactionViewModel
import net.micode.moneytracker.viewmodel.AccountViewModel
import net.micode.moneytracker.viewmodel.TransactionViewModelFactory
import net.micode.moneytracker.repository.TransactionRepository
import net.micode.moneytracker.ui.theme.SpendingTrackerTheme
import net.micode.moneytracker.util.SettingsManager

/**
 * Main activity of the application.
 * It serves as the entry point for the UI, handling theme initialization,
 * security locking (passcode), and hosting the main Dashboard.
 */
class MainActivity : AppCompatActivity() {
    
    private val settingsManager: SettingsManager by lazy { SettingsManager(applicationContext) }

    private val factory by lazy {
        val app = application as SpendingTrackerApp
        val repository = TransactionRepository.getInstance(
            app.database.transactionDao(),
            app.database.categoryDao(),
            app.database.periodSummaryDao(),
            app.database.accountDao()
        )
        TransactionViewModelFactory(repository, settingsManager)
    }

    private val viewModel: TransactionViewModel by viewModels { factory }
    private val accountViewModel: AccountViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            SpendingTrackerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val isPasscodeEnabled = remember { settingsManager.isPasscodeEnabled() }
                    val correctPin = remember { settingsManager.getPasscode() }
                    
                    var isLocked by rememberSaveable { mutableStateOf(isPasscodeEnabled) }

                    if (isLocked) {
                        LockScreen(
                            correctPin = correctPin,
                            onUnlocked = { isLocked = false }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            viewModel.refreshCurrency()
                        }
                        DashboardScreen(
                            viewModel = viewModel,
                            accountViewModel = accountViewModel
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure currency symbol is up to date when returning to the app
        viewModel.refreshCurrency()
    }
}
