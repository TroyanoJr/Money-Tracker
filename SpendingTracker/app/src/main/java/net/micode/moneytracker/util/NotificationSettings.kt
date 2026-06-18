package net.micode.moneytracker.util

import android.content.SharedPreferences

/**
 * Manages notification and reminder preferences.
 * Part of the SettingsManager refactor to comply with SRP.
 */
class NotificationSettings(private val prefs: SharedPreferences) {

    companion object {
        const val KEY_REMINDER_FREQUENCY = "reminder_frequency"
        const val KEY_REMINDER_HOUR = "reminder_hour"
        const val KEY_REMINDER_MINUTE = "reminder_minute"
    }

    /** Retrieves the frequency of reminders. */
    fun getReminderFrequency(): String = prefs.getString(KEY_REMINDER_FREQUENCY, "Every Day") ?: "Every Day"
    
    /** Sets the frequency of reminders. */
    fun setReminderFrequency(frequency: String) = prefs.edit().putString(KEY_REMINDER_FREQUENCY, frequency).apply()

    /** Gets the configured reminder time as a Pair of (Hour, Minute). */
    fun getReminderTime(): Pair<Int, Int> = Pair(prefs.getInt(KEY_REMINDER_HOUR, 20), prefs.getInt(KEY_REMINDER_MINUTE, 0))
    
    /** Sets the reminder time. */
    fun setReminderTime(hour: Int, minute: Int) = prefs.edit().putInt(KEY_REMINDER_HOUR, hour).putInt(KEY_REMINDER_MINUTE, minute).apply()
}
