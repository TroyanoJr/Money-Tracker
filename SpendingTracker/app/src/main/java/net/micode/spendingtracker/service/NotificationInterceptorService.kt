package net.micode.spendingtracker.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import java.util.regex.Pattern

/**
 * Service that listens for system notifications.
 * Filters notifications from Alipay and WeChat, detects payments in Chinese,
 * and extracts the amount and transaction type.
 */
class NotificationInterceptorService : NotificationListenerService() {

    private val alipayPackage = "com.eg.android.AlipayGphone"
    private val wechatPackage = "com.tencent.mm"

    // Keywords for Chinese payment notifications
    private val expenseKeywords = listOf("支付成功", "付款", "支出", "消费")
    private val incomeKeywords = listOf("收款", "到账", "收入")

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        val packageName = sbn?.packageName ?: return
        if (packageName != alipayPackage && packageName != wechatPackage) return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getString(Notification.EXTRA_TEXT) ?: ""
        val fullContent = "$title $text"

        val appName = if (packageName == alipayPackage) "Alipay" else "WeChat"
        
        processNotification(appName, fullContent)
    }

    private fun processNotification(appName: String, content: String) {
        // 1. Detect Amount using Regex
        // Pattern matches numbers like 10, 10.5, 1,000.00 following ¥ or before 元
        val amountPattern = Pattern.compile("(?<=¥|金额|付款|收款|支出|收入)\\s*([0-9,.]+)|([0-9,.]+)(?=\\s*元)")
        val matcher = amountPattern.matcher(content)
        
        var amount = ""
        if (matcher.find()) {
            amount = matcher.group().trim().replace(",", "")
        }

        if (amount.isEmpty()) return // Not a payment notification or amount not found

        // 2. Detect Transaction Type
        val isExpense = expenseKeywords.any { content.contains(it) }
        val isIncome = incomeKeywords.any { content.contains(it) }

        val type = when {
            isExpense -> "Gasto (Expense)"
            isIncome -> "Ingreso (Income)"
            else -> return // Could not determine type definitively
        }

        // 3. Notify user
        showToast("$appName: $type detectado de ¥$amount")
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }
}
