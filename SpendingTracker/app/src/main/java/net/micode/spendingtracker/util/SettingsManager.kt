package net.micode.spendingtracker.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages app settings and persistence using SharedPreferences.
 */
class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        const val KEY_AUTO_CAPTURE = "auto_capture_notifications"
    }

    /**
     * Set the status of automatic notification capture.
     */
    fun setAutoCaptureEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CAPTURE, enabled).apply()
    }

    /**
     * Get the current status of automatic notification capture.
     * Defaults to true.
     */
    fun isAutoCaptureEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_CAPTURE, true)
    }
}
