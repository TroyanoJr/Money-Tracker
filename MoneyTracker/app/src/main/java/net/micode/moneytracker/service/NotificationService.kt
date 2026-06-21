package net.micode.moneytracker.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import net.micode.moneytracker.util.ReminderManager

/**
 * Foreground Service that ensures reminders are delivered even if the application is closed.
 * This service runs with a low-priority notification to comply with Android's foreground
 * service requirements while maintaining the reliability of scheduled alarms.
 */
class NotificationService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Initializes the service and starts it in the foreground.
     * It also triggers the [ReminderManager] to ensure alarms are properly scheduled.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "spending_tracker_service"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminder Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Reminders Active")
            .setContentText("Keeping your spending reminders ready...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Start as foreground service to prevent the system from killing the process
        startForeground(1, notification)

        // Reschedule alarm from service context
        ReminderManager.scheduleReminder(this)

        return START_STICKY
    }
}
