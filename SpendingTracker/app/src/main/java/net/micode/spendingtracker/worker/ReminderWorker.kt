package net.micode.spendingtracker.worker

import android.content.Context
import androidx.work.*
import net.micode.spendingtracker.util.ReminderManager
import java.util.concurrent.TimeUnit

/**
 * Worker that runs periodically to ensure the system alarm is always scheduled.
 * This is the professional way to handle aggressive battery optimization in production.
 */
class ReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // Re-schedule the alarm just in case the system killed it
        ReminderManager.scheduleReminder(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "ReminderWatchdog"

        fun startWatchdog(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
