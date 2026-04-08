package net.micode.spendingtracker.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages app settings and persistence using SharedPreferences.
 * Now includes Premium status management.
 */
class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        const val KEY_CURRENCY_CODE = "currency_code"
        const val KEY_HAS_SEEDED_CATEGORIES = "has_seeded_categories"
        const val KEY_PASSCODE_ENABLED = "passcode_enabled"
        const val KEY_LOCK_TYPE = "lock_type"
        const val KEY_PASSCODE_VALUE = "passcode_value"
        const val KEY_BUDGET_MODE_ENABLED = "budget_mode_enabled"
        const val KEY_MONTHLY_BUDGET_AMOUNT = "monthly_budget_amount"
        const val KEY_INCLUDE_INCOME_IN_BUDGET = "include_income_in_budget"
        const val KEY_REMINDER_FREQUENCY = "reminder_frequency"
        const val KEY_REMINDER_HOUR = "reminder_hour"
        const val KEY_REMINDER_MINUTE = "reminder_minute"
        const val KEY_IS_PREMIUM = "is_premium_user"
    }

    /**
     * Premium status management.
     */
    fun isPremium(): Boolean {
        return prefs.getBoolean(KEY_IS_PREMIUM, false)
    }

    fun setPremiumStatus(isPremium: Boolean) {
        prefs.edit().putBoolean(KEY_IS_PREMIUM, isPremium).apply()
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

    fun isPasscodeEnabled(): Boolean {
        return prefs.getBoolean(KEY_PASSCODE_ENABLED, false)
    }

    fun setPasscodeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PASSCODE_ENABLED, enabled).apply()
    }

    fun getLockType(): String {
        return prefs.getString(KEY_LOCK_TYPE, "PIN") ?: "PIN"
    }

    fun setLockType(type: String) {
        prefs.edit().putString(KEY_LOCK_TYPE, type).apply()
    }

    fun getPasscode(): String {
        return prefs.getString(KEY_PASSCODE_VALUE, "") ?: ""
    }

    fun setPasscode(passcode: String) {
        prefs.edit().putString(KEY_PASSCODE_VALUE, passcode).apply()
    }

    fun isBudgetModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_BUDGET_MODE_ENABLED, false)
    }

    fun setBudgetModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BUDGET_MODE_ENABLED, enabled).apply()
    }

    fun getMonthlyBudget(): Double {
        return prefs.getFloat(KEY_MONTHLY_BUDGET_AMOUNT, 0f).toDouble()
    }

    fun setMonthlyBudget(amount: Double) {
        prefs.edit().putFloat(KEY_MONTHLY_BUDGET_AMOUNT, amount.toFloat()).apply()
    }

    fun isIncludeIncomeInBudget(): Boolean {
        return prefs.getBoolean(KEY_INCLUDE_INCOME_IN_BUDGET, false)
    }

    fun setIncludeIncomeInBudget(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_INCLUDE_INCOME_IN_BUDGET, enabled).apply()
    }

    fun getReminderFrequency(): String {
        return prefs.getString(KEY_REMINDER_FREQUENCY, "Every Day") ?: "Every Day"
    }

    fun setReminderFrequency(frequency: String) {
        prefs.edit().putString(KEY_REMINDER_FREQUENCY, frequency).apply()
    }

    fun getReminderTime(): Pair<Int, Int> {
        return Pair(
            prefs.getInt(KEY_REMINDER_HOUR, 20), // Default 8 PM
            prefs.getInt(KEY_REMINDER_MINUTE, 0)
        )
    }

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()
    }
}
