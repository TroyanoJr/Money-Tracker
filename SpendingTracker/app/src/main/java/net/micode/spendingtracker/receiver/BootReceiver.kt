package net.micode.spendingtracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.micode.spendingtracker.util.ReminderManager

/**
 * BroadcastReceiver that listens for system boot events.
 * It ensures that app reminders are re-scheduled after the device is restarted.
 */
class BootReceiver : BroadcastReceiver() {
    /**
     * Called when the BroadcastReceiver is receiving an Intent broadcast.
     * Triggers the [ReminderManager] to schedule the notification task.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            // Re-schedule reminder after phone reboot
            ReminderManager.scheduleReminder(context)
        }
    }
}
