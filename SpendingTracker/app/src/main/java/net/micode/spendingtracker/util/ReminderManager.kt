package net.micode.spendingtracker.util

import android.content.Context
import android.util.Log
import androidx.work.*
import net.micode.spendingtracker.worker.ReminderWorker
import java.util.*
import java.util.concurrent.TimeUnit

object ReminderManager {
    private const val TAG = "ReminderManager"
    private const val WORK_NAME = "one_time_reminder_work"

    fun scheduleReminder(context: Context) {
        val settingsManager = SettingsManager(context)
        val frequency = settingsManager.getReminderFrequency()
        
        if (frequency == "Never") {
            Log.d(TAG, "Reminder disabled by user (Frequency: Never)")
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            return
        }

        val (hour, minute) = settingsManager.getReminderTime()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = Calendar.getInstance()
        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = calendar.timeInMillis - now.timeInMillis
        Log.d(TAG, "Scheduling reminder in ${delay / 1000} seconds (at ${hour}:${minute})")

        // Using OneTimeWorkRequest for better precision and self-rescheduling
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(WORK_NAME)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
