package net.micode.spendingtracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.micode.spendingtracker.util.ReminderManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule reminder after phone reboot
            ReminderManager.scheduleReminder(context)
        }
    }
}
