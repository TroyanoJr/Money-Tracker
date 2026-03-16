package net.micode.spendingtracker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import net.micode.spendingtracker.ui.screens.DashboardScreen
import net.micode.spendingtracker.viewmodel.TransactionViewModel
import net.micode.spendingtracker.viewmodel.TransactionViewModelFactory
import net.micode.spendingtracker.repository.TransactionRepository
import net.micode.spendingtracker.ui.theme.SpendingTrackerTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: TransactionViewModel by viewModels {
        val app = application as SpendingTrackerApp
        val repository = TransactionRepository.getInstance(
            app.database.transactionDao(),
            app.database.categoryDao()
        )
        TransactionViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkNotificationPermission()

        setContent {
            SpendingTrackerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkNotificationPermission(onlyShowIfDisabled = true)
    }

    private fun checkNotificationPermission(onlyShowIfDisabled: Boolean = false) {
        val packageName = packageName
        val listeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val isEnabled = listeners?.contains(packageName) == true

        if (isEnabled) {
            if (!onlyShowIfDisabled) {
                Toast.makeText(this, "Servicio de monitoreo activo ✅", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Por favor, activa el acceso a notificaciones para Spending Tracker ⚠️", Toast.LENGTH_LONG).show()
            try {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
    }
}
