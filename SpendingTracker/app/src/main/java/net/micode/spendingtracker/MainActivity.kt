package net.micode.spendingtracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.content.ContextCompat
import net.micode.spendingtracker.ui.screens.DashboardScreen
import net.micode.spendingtracker.ui.screens.LockScreen
import net.micode.spendingtracker.viewmodel.TransactionViewModel
import net.micode.spendingtracker.viewmodel.TransactionViewModelFactory
import net.micode.spendingtracker.repository.TransactionRepository
import net.micode.spendingtracker.ui.theme.SpendingTrackerTheme
import net.micode.spendingtracker.util.SettingsManager

class MainActivity : ComponentActivity() {
    
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkAndRequestNotificationPermission()

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

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshCurrency()
    }
}
