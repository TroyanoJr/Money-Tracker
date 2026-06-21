package net.micode.moneytracker.worker

import android.content.Context
import androidx.work.*
import net.micode.moneytracker.util.ReminderManager
import java.util.concurrent.TimeUnit

class ReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // Esta es la clave: el Worker despierta la app y el ReminderManager vuelve a poner la alarma exacta
        ReminderManager.scheduleReminder(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "ReminderWatchdog"

        fun startWatchdog(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
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
