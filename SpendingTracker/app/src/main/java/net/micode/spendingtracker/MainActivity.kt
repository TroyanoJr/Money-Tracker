package net.micode.spendingtracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import net.micode.spendingtracker.ui.screens.DashboardScreen
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
            app.database.categoryDao()
        )
        TransactionViewModelFactory(repository, settingsManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkNotificationPermission()

        setContent {
            SpendingTrackerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    // Refrescar moneda cuando la pantalla vuelve a estar activa
                    LaunchedEffect(Unit) {
                        viewModel.refreshCurrency()
                    }
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkNotificationPermission(onlyShowIfDisabled = true)
        viewModel.refreshCurrency()
    }

    private fun checkNotificationPermission(onlyShowIfDisabled: Boolean = false) {
        val packageName = packageName
        val listeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val isEnabled = listeners?.contains(packageName) == true
        
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val hasShownActiveToast = prefs.getBoolean("has_shown_active_toast", false)

        if (isEnabled) {
            if (!hasShownActiveToast) {
                Toast.makeText(this, "Monitoring service active ✅", Toast.LENGTH_SHORT).show()
                prefs.edit().putBoolean("has_shown_active_toast", true).apply()
            }
        } else {
            // Reset flag so it shows again once the user enables it
            prefs.edit().putBoolean("has_shown_active_toast", false).apply()
            
            if (!onlyShowIfDisabled || !hasShownActiveToast) {
                Toast.makeText(this, "Please enable notification access for Spending Tracker ⚠️", Toast.LENGTH_LONG).show()
                try {
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                } catch (e: Exception) {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        }
    }
}
