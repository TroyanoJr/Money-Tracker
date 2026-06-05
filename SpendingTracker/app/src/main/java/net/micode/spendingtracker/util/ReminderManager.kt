package net.micode.spendingtracker.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import net.micode.spendingtracker.receiver.ReminderReceiver
import java.util.*

/**
 * Utility object for managing the scheduling of app reminders.
 * It uses [AlarmManager] to trigger notifications at specific times, with optimizations
 * for different Android versions and specific hardware manufacturers (like Huawei).
 */
object ReminderManager {
    private const val REQUEST_CODE = 1001

    /**
     * Schedules a daily reminder based on the user's preferences stored in [SettingsManager].
     * Uses exact alarms when possible to ensure timely notifications.
     * 
     * @param context Application context.
     */
    fun scheduleReminder(context: Context) {
        val settingsManager = SettingsManager(context)
        val frequency = settingsManager.getReminderFrequency()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Cancel if frequency is set to "Never"
        if (frequency == "Never") {
            alarmManager.cancel(pendingIntent)
            return
        }

        val (hour, minute) = settingsManager.getReminderTime()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            // Use exact alarms based on API level compatibility and permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            // Fallback to non-exact alarm if exact alarm fails
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    /**
     * Checks if the app needs battery optimization exemption, specifically for Huawei and Honor devices
     * which are known for aggressive background task management.
     * 
     * @param context Application context.
     * @return True if exemption is recommended but not yet granted.
     */
    fun needsBatteryExemption(context: Context): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val isAggressive = manufacturer.contains("huawei") || manufacturer.contains("honor")
        if (!isAggressive) return false

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            !powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else false
    }
}
