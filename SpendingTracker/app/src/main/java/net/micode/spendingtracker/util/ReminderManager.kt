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
 * Manages the scheduling of reminders.
 * Optimized for standard Android flow while supporting Huawei manual instructions.
 */
object ReminderManager {
    private const val REQUEST_CODE = 1001

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
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            // Use exact alarms based on API level compatibility
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
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    /**
     * Specifically identifies Huawei/Honor for manual battery instruction card.
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
