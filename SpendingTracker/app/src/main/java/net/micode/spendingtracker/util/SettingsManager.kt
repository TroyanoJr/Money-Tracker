package net.micode.spendingtracker.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages app settings and persistence using SharedPreferences.
 * Supports account-specific settings for the Spending section.
 */
class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        const val KEY_CURRENCY_CODE = "currency_code"
        const val KEY_HAS_SEEDED_CATEGORIES = "has_seeded_categories"
        const val KEY_PASSCODE_ENABLED = "passcode_enabled"
        const val KEY_FACE_UNLOCK_ENABLED = "face_unlock_enabled"
        const val KEY_FINGERPRINT_ENABLED = "fingerprint_enabled"
        const val KEY_LOCK_TYPE = "lock_type"
        const val KEY_PASSCODE_VALUE = "passcode_value"
        const val KEY_BUDGET_MODE_ENABLED = "budget_mode_enabled"
        const val KEY_MONTHLY_BUDGET_AMOUNT = "monthly_budget_amount"
        const val KEY_INCLUDE_INCOME_IN_BUDGET = "include_income_in_budget"
        const val KEY_CARRY_OVER_ENABLED = "carry_over_enabled"
        const val KEY_CARRY_OVER_POSITIVE_ONLY = "carry_over_positive_only"
        const val KEY_CARRY_OVER_ADD_TO_INCOME = "carry_over_add_to_income"
        const val KEY_REMINDER_FREQUENCY = "reminder_frequency"
        const val KEY_REMINDER_HOUR = "reminder_hour"
        const val KEY_REMINDER_MINUTE = "reminder_minute"
        const val KEY_IS_PREMIUM = "is_premium_user"
        const val KEY_LANGUAGE = "language_pref"
    }

    private fun userKey(key: String, accountId: Long): String = "${accountId}_$key"

    fun isPremium(): Boolean = prefs.getBoolean(KEY_IS_PREMIUM, false)
    fun setPremiumStatus(isPremium: Boolean) = prefs.edit().putBoolean(KEY_IS_PREMIUM, isPremium).apply()

    fun setCurrencySymbol(symbol: String) = prefs.edit().putString(KEY_CURRENCY_SYMBOL, symbol).apply()
    fun getCurrencySymbol(): String = prefs.getString(KEY_CURRENCY_SYMBOL, "¥") ?: "¥"

    fun setCurrencyCode(code: String) = prefs.edit().putString(KEY_CURRENCY_CODE, code).apply()
    fun getCurrencyCode(): String = prefs.getString(KEY_CURRENCY_CODE, "CNY") ?: "CNY"

    fun setHasSeededCategories(hasSeeded: Boolean) = prefs.edit().putBoolean(KEY_HAS_SEEDED_CATEGORIES, hasSeeded).apply()
    fun hasSeededCategories(): Boolean = prefs.getBoolean(KEY_HAS_SEEDED_CATEGORIES, false)

    fun isPasscodeEnabled(): Boolean = prefs.getBoolean(KEY_PASSCODE_ENABLED, false)
    fun setPasscodeEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_PASSCODE_ENABLED, enabled).apply()

    fun isFaceUnlockEnabled(): Boolean = prefs.getBoolean(KEY_FACE_UNLOCK_ENABLED, false)
    fun setFaceUnlockEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_FACE_UNLOCK_ENABLED, enabled).apply()

    fun isFingerprintEnabled(): Boolean = prefs.getBoolean(KEY_FINGERPRINT_ENABLED, false)
    fun setFingerprintEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_FINGERPRINT_ENABLED, enabled).apply()

    fun getLockType(): String = prefs.getString(KEY_LOCK_TYPE, "PIN") ?: "PIN"
    fun setLockType(type: String) = prefs.edit().putString(KEY_LOCK_TYPE, type).apply()

    fun getPasscode(): String = prefs.getString(KEY_PASSCODE_VALUE, "") ?: ""
    fun setPasscode(passcode: String) = prefs.edit().putString(KEY_PASSCODE_VALUE, passcode).apply()

    // --- Account Specific Spending Settings ---

    fun isBudgetModeEnabled(accountId: Long): Boolean = 
        prefs.getBoolean(userKey(KEY_BUDGET_MODE_ENABLED, accountId), false)
    
    fun setBudgetModeEnabled(accountId: Long, enabled: Boolean) = 
        prefs.edit().putBoolean(userKey(KEY_BUDGET_MODE_ENABLED, accountId), enabled).apply()

    fun getMonthlyBudget(accountId: Long): Double = 
        prefs.getFloat(userKey(KEY_MONTHLY_BUDGET_AMOUNT, accountId), 0f).toDouble()
    
    fun setMonthlyBudget(accountId: Long, amount: Double) = 
        prefs.edit().putFloat(userKey(KEY_MONTHLY_BUDGET_AMOUNT, accountId), amount.toFloat()).apply()

    fun isIncludeIncomeInBudget(accountId: Long): Boolean = 
        prefs.getBoolean(userKey(KEY_INCLUDE_INCOME_IN_BUDGET, accountId), false)
    
    fun setIncludeIncomeInBudget(accountId: Long, enabled: Boolean) = 
        prefs.edit().putBoolean(userKey(KEY_INCLUDE_INCOME_IN_BUDGET, accountId), enabled).apply()

    fun isCarryOverEnabled(accountId: Long): Boolean = 
        prefs.getBoolean(userKey(KEY_CARRY_OVER_ENABLED, accountId), false)
    
    fun setCarryOverEnabled(accountId: Long, enabled: Boolean) = 
        prefs.edit().putBoolean(userKey(KEY_CARRY_OVER_ENABLED, accountId), enabled).apply()

    fun isCarryOverPositiveOnly(accountId: Long): Boolean = 
        prefs.getBoolean(userKey(KEY_CARRY_OVER_POSITIVE_ONLY, accountId), false)
    
    fun setCarryOverPositiveOnly(accountId: Long, enabled: Boolean) = 
        prefs.edit().putBoolean(userKey(KEY_CARRY_OVER_POSITIVE_ONLY, accountId), enabled).apply()

    fun isCarryOverAddToIncome(accountId: Long): Boolean = 
        prefs.getBoolean(userKey(KEY_CARRY_OVER_ADD_TO_INCOME, accountId), false)
    
    fun setCarryOverAddToIncome(accountId: Long, enabled: Boolean) = 
        prefs.edit().putBoolean(userKey(KEY_CARRY_OVER_ADD_TO_INCOME, accountId), enabled).apply()

    // --- General Settings ---

    fun getReminderFrequency(): String = prefs.getString(KEY_REMINDER_FREQUENCY, "Every Day") ?: "Every Day"
    fun setReminderFrequency(frequency: String) = prefs.edit().putString(KEY_REMINDER_FREQUENCY, frequency).apply()

    fun getReminderTime(): Pair<Int, Int> = Pair(prefs.getInt(KEY_REMINDER_HOUR, 20), prefs.getInt(KEY_REMINDER_MINUTE, 0))
    fun setReminderTime(hour: Int, minute: Int) = prefs.edit().putInt(KEY_REMINDER_HOUR, hour).putInt(KEY_REMINDER_MINUTE, minute).apply()

    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "System") ?: "System"
    fun setLanguage(language: String) = prefs.edit().putString(KEY_LANGUAGE, language).apply()
}
