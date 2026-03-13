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
import net.micode.spendingtracker.util.PaymentParser
import java.util.UUID

/**
 * Service that listens for system notifications.
 * Filters notifications from Alipay, WeChat, and ADB Shell.
 * Automatically adds detected transactions to the repository.
 */
class NotificationInterceptorService : NotificationListenerService() {

    private val alipayPackage = "com.eg.android.AlipayGphone"
    private val wechatPackage = "com.tencent.mm"
    private val shellPackage = "com.android.shell"
    
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        val packageName = sbn?.packageName ?: return
        
        // Filter: Only process Alipay, WeChat, and ADB Shell
        if (packageName != alipayPackage && packageName != wechatPackage && packageName != shellPackage) return

        val extras = sbn.notification.extras
        
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
        
        val fullContent = "$title $text $bigText $subText"

        val result = PaymentParser.parse(fullContent, sbn.postTime)
        
        if (result != null) {
            val appName = when (packageName) {
                alipayPackage -> "Alipay"
                wechatPackage -> "WeChat"
                else -> "ADB Test"
            }

            // Map category name to icon
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
            val finalNote = "${notePrefix}Auto-detectado ($appName)"

            val newTransaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = result.amount,
                categoryName = result.category,
                categoryIcon = icon,
                date = result.timestamp,
                note = finalNote,
                isExpense = result.isExpense
            )

            // Persist to database
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
