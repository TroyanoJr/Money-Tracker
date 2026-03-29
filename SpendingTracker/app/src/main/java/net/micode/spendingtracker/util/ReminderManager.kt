package net.micode.spendingtracker.util

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

object ReminderManager {
    private const val TAG = "ReminderManager"
    private const val REQUEST_CODE = 1001

    fun scheduleReminder(context: Context) {
        val settingsManager = SettingsManager(context)
        val frequency = settingsManager.getReminderFrequency()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

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
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm", e)
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    fun isBatteryOptimized(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            !powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else false
    }

    fun openBatterySettings(context: Context) {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val intent = Intent()
        try {
            if (manufacturer.contains("huawei") || manufacturer.contains("honor")) {
                // INTENT ESPECÍFICO PARA HARMONYOS 4.2 / EMUI 12+ (Startup Manager)
                intent.component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")
                if (isIntentCallable(context, intent)) {
                    context.startActivity(intent)
                    return
                }
                // Fallback para versiones anteriores de Huawei
                intent.component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
            } else {
                intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(fallbackIntent)
        }
    }

    private fun isIntentCallable(context: Context, intent: Intent): Boolean {
        val list = context.packageManager.queryIntentActivities(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
        return list.size > 0
    }
}
