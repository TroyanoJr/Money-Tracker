package net.micode.spendingtracker.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.micode.spendingtracker.SpendingTrackerApp
import net.micode.spendingtracker.model.Transaction
import net.micode.spendingtracker.repository.TransactionRepository
import net.micode.spendingtracker.util.PaymentAppConfig
import net.micode.spendingtracker.util.PaymentParser
import net.micode.spendingtracker.util.SettingsManager
import java.util.UUID

/**
 * Service that listens for system notifications.
 * Automatically adds detected transactions to the repository.
 * Marks transactions as incomplete if merchant or category is missing.
 */
class NotificationInterceptorService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        val settingsManager = SettingsManager(applicationContext)
        if (!settingsManager.isAutoCaptureEnabled()) return

        val packageName = sbn?.packageName ?: return
        if (!PaymentAppConfig.isSupported(packageName)) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
        
        val fullContent = "$title $text $bigText $subText"
        val result = PaymentParser.parse(fullContent, sbn.postTime)
        
        if (result != null) {
            val appName = PaymentAppConfig.getAppName(packageName)

            val icon = if (result.isExpense) {
                when (result.category) {
                    "Food & Dining" -> Icons.Default.Restaurant
                    "Shopping" -> Icons.Default.ShoppingCart
                    "Transport" -> Icons.Default.DirectionsBus
                    "Utilities & Subs" -> Icons.Default.Wifi
                    "Health & Beauty" -> Icons.Default.HealthAndSafety
                    "Entertainment" -> Icons.Default.Movie
                    "Education" -> Icons.Default.School
                    else -> Icons.Default.Sell
                }
            } else {
                Icons.Default.Payments
            }

            val notePrefix = if (result.merchant != null) "${result.merchant} - " else ""
            val finalNote = "${notePrefix}Auto-detected ($appName)"

            val newTransaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = result.amount,
                categoryName = if (result.isComplete) result.category else "Pending",
                categoryIcon = icon,
                date = result.timestamp,
                note = finalNote,
                isExpense = result.isExpense,
                isComplete = result.isComplete
            )

            serviceScope.launch {
                val database = (application as SpendingTrackerApp).database
                val repository = TransactionRepository.getInstance(
                    database.transactionDao(),
                    database.categoryDao()
                )
                repository.insertTransaction(newTransaction)
            }
        }
    }
}
