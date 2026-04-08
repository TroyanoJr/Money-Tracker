package net.micode.spendingtracker.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import net.micode.spendingtracker.receiver.ReminderReceiver
import java.util.*

/**
 * Manages the scheduling of reminders and handles battery optimization exemptions
 * across different Android versions and manufacturers.
 */
object ReminderManager {
    private const val TAG = "ReminderManager"
    private const val REQUEST_CODE = 1001

    /**
     * Schedules an exact alarm using the best available API.
     * Targeted at ReminderReceiver via explicit intent to ensure it works when closed.
     */
    fun scheduleReminder(context: Context) {
        val settingsManager = SettingsManager(context)
        val frequency = settingsManager.getReminderFrequency()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Explicit intent is the most reliable way to wake up a closed app
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            component = ComponentName(context.packageName, ReminderReceiver::class.java.name)
        }
        
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
            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            // Using setExactAndAllowWhileIdle to bypass Doze mode restrictions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
            Log.d(TAG, "Reminder successfully scheduled for ${calendar.time}")
        } catch (e: Exception) {
            Log.e(TAG, "Alarm scheduling failed, falling back to standard set", e)
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    /**
     * Determines if the specific device manufacturer requires manual intervention
     * to keep alarms alive after the app is swiped away.
     */
    fun needsBatteryExemption(context: Context): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        // Brands known for killing background processes aggressively
        val aggressiveBrands = listOf("huawei", "honor", "xiaomi", "oppo", "vivo", "samsung")
        val isAggressive = aggressiveBrands.any { manufacturer.contains(it) }
        
        // If it's an aggressive brand and still optimized, we need to show the warning
        return if (isAggressive) isBatteryOptimized(context) else false
    }

    /**
     * Intelligent check: Determines if the app is optimized (restricted) by the system.
     */
    fun isBatteryOptimized(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            !powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else false
    }

    /**
     * Professional Redirection:
     * - Huawei/Honor: Direct access to "App Launch" screen (Startup Manager).
     * - Standard Android: Official one-tap direct system dialog.
     */
    @SuppressLint("BatteryLife")
    fun openBatterySettings(context: Context) {
        val manufacturer = Build.MANUFACTURER.lowercase()
        
        // Specifically targeting Huawei's Startup Management to prevent task-kill issues
        if (manufacturer.contains("huawei") || manufacturer.contains("honor")) {
            try {
                val intent = Intent().apply {
                    component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return 
            } catch (e: Exception) {
                // Fallback for different EMUI versions
                try {
                    val intent = Intent().apply {
                        component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return
                } catch (e2: Exception) { }
            }
        }

        // Standard Android approach: Direct One-Tap System Prompt
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to general battery list if direct prompt fails
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }
    }
}
