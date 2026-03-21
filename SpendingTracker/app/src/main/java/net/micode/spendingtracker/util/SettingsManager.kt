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
        const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        const val KEY_CURRENCY_CODE = "currency_code"
        const val KEY_HAS_SEEDED_CATEGORIES = "has_seeded_categories"
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

    /**
     * Set the global currency symbol (e.g., "$", "€", "¥").
     */
    fun setCurrencySymbol(symbol: String) {
        prefs.edit().putString(KEY_CURRENCY_SYMBOL, symbol).apply()
    }

    /**
     * Get the global currency symbol. Defaults to "¥".
     */
    fun getCurrencySymbol(): String {
        return prefs.getString(KEY_CURRENCY_SYMBOL, "¥") ?: "¥"
    }

    /**
     * Set the global currency code (e.g., "USD", "EUR", "CNY").
     */
    fun setCurrencyCode(code: String) {
        prefs.edit().putString(KEY_CURRENCY_CODE, code).apply()
    }

    /**
     * Get the global currency code. Defaults to "CNY".
     */
    fun getCurrencyCode(): String {
        return prefs.getString(KEY_CURRENCY_CODE, "CNY") ?: "CNY"
    }

    /**
     * Flag to ensure default categories are only seeded once.
     */
    fun setHasSeededCategories(hasSeeded: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_SEEDED_CATEGORIES, hasSeeded).apply()
    }

    fun hasSeededCategories(): Boolean {
        return prefs.getBoolean(KEY_HAS_SEEDED_CATEGORIES, false)
    }
}
