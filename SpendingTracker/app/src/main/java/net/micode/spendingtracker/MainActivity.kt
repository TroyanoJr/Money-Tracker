package net.micode.spendingtracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import net.micode.spendingtracker.ui.screens.DashboardScreen
import net.micode.spendingtracker.ui.screens.LockScreen
import net.micode.spendingtracker.viewmodel.TransactionViewModel
import net.micode.spendingtracker.viewmodel.TransactionViewModelFactory
import net.micode.spendingtracker.repository.TransactionRepository
import net.micode.spendingtracker.ui.theme.SpendingTrackerTheme
import net.micode.spendingtracker.util.SettingsManager

/**
 * Main entry point of the application.
 * Using AppCompatActivity to support dynamic language switching.
 */
class MainActivity : AppCompatActivity() {
    
    private val settingsManager: SettingsManager by lazy { SettingsManager(applicationContext) }

    private val viewModel: TransactionViewModel by viewModels {
        val app = application as SpendingTrackerApp
        val repository = TransactionRepository.getInstance(
            app.database.transactionDao(),
            app.database.categoryDao(),
            app.database.periodSummaryDao()
        )
        TransactionViewModelFactory(repository, settingsManager)
    }

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
                        DashboardScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshCurrency()
    }
}
