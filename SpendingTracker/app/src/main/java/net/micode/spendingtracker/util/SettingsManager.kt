package net.micode.spendingtracker.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages application-wide and account-specific settings using [SharedPreferences].
 * Provides a centralized way to access and modify user preferences such as currency,
 * security options, budget configurations, and reminders.
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

    /**
     * Generates a unique preference key for account-specific settings.
     */
    private fun userKey(key: String, accountId: Long): String = "${accountId}_$key"

    /** Returns true if the user has premium status. */
    fun isPremium(): Boolean = prefs.getBoolean(KEY_IS_PREMIUM, false)
    
    /** Sets the premium status for the user. */
    fun setPremiumStatus(isPremium: Boolean) = prefs.edit().putBoolean(KEY_IS_PREMIUM, isPremium).apply()

    /** Sets the currency symbol (e.g., "$", "€", "¥"). */
    fun setCurrencySymbol(symbol: String) = prefs.edit().putString(KEY_CURRENCY_SYMBOL, symbol).apply()
    
    /** Retrieves the current currency symbol. Defaults to "¥". */
    fun getCurrencySymbol(): String = prefs.getString(KEY_CURRENCY_SYMBOL, "¥") ?: "¥"

    /** Sets the ISO currency code (e.g., "USD", "EUR"). */
    fun setCurrencyCode(code: String) = prefs.edit().putString(KEY_CURRENCY_CODE, code).apply()
    
    /** Retrieves the current currency code. Defaults to "CNY". */
    fun getCurrencyCode(): String = prefs.getString(KEY_CURRENCY_CODE, "CNY") ?: "CNY"

    /** Marks whether default categories have been seeded into the database. */
    fun setHasSeededCategories(hasSeeded: Boolean) = prefs.edit().putBoolean(KEY_HAS_SEEDED_CATEGORIES, hasSeeded).apply()
    
    /** Checks if default categories have been seeded. */
    fun hasSeededCategories(): Boolean = prefs.getBoolean(KEY_HAS_SEEDED_CATEGORIES, false)

    /** Returns true if passcode protection is enabled. */
    fun isPasscodeEnabled(): Boolean = prefs.getBoolean(KEY_PASSCODE_ENABLED, false)
    
    /** Enables or disables passcode protection. */
    fun setPasscodeEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_PASSCODE_ENABLED, enabled).apply()

    /** Returns true if face unlock is enabled. */
    fun isFaceUnlockEnabled(): Boolean = prefs.getBoolean(KEY_FACE_UNLOCK_ENABLED, false)
    
    /** Enables or disables face unlock. */
    fun setFaceUnlockEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_FACE_UNLOCK_ENABLED, enabled).apply()

    /** Returns true if fingerprint unlock is enabled. */
    fun isFingerprintEnabled(): Boolean = prefs.getBoolean(KEY_FINGERPRINT_ENABLED, false)
    
    /** Enables or disables fingerprint unlock. */
    fun setFingerprintEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_FINGERPRINT_ENABLED, enabled).apply()

    /** Gets the current lock type (e.g., "PIN", "BIOMETRIC"). */
    fun getLockType(): String = prefs.getString(KEY_LOCK_TYPE, "PIN") ?: "PIN"
    
    /** Sets the lock type for security. */
    fun setLockType(type: String) = prefs.edit().putString(KEY_LOCK_TYPE, type).apply()

    /** Retrieves the stored passcode. */
    fun getPasscode(): String = prefs.getString(KEY_PASSCODE_VALUE, "") ?: ""
    
    /** Sets the passcode value. */
    fun setPasscode(passcode: String) = prefs.edit().putString(KEY_PASSCODE_VALUE, passcode).apply()

    // --- Account Specific Spending Settings ---

    /** Checks if budget mode is enabled for a specific account. */
    fun isBudgetModeEnabled(accountId: Long): Boolean = 
        prefs.getBoolean(userKey(KEY_BUDGET_MODE_ENABLED, accountId), false)
    
    /** Sets budget mode status for a specific account. */
    fun setBudgetModeEnabled(accountId: Long, enabled: Boolean) = 
        prefs.edit().putBoolean(userKey(KEY_BUDGET_MODE_ENABLED, accountId), enabled).apply()

    /** Gets the monthly budget amount for a specific account. */
    fun getMonthlyBudget(accountId: Long): Double = 
        prefs.getFloat(userKey(KEY_MONTHLY_BUDGET_AMOUNT, accountId), 0f).toDouble()
    
    /** Sets the monthly budget amount for a specific account. */
    fun setMonthlyBudget(accountId: Long, amount: Double) = 
        prefs.edit().putFloat(userKey(KEY_MONTHLY_BUDGET_AMOUNT, accountId), amount.toFloat()).apply()

    /** Checks if income should be included in the budget calculation for an account. */
    fun isIncludeIncomeInBudget(accountId: Long): Boolean = 
        prefs.getBoolean(userKey(KEY_INCLUDE_INCOME_IN_BUDGET, accountId), false)
    
    /** Sets whether income is included in the budget for an account. */
    fun setIncludeIncomeInBudget(accountId: Long, enabled: Boolean) = 
        prefs.edit().putBoolean(userKey(KEY_INCLUDE_INCOME_IN_BUDGET, accountId), enabled).apply()

    /** Checks if carry-over is enabled for an account. */
    fun isCarryOverEnabled(accountId: Long): Boolean = 
        prefs.getBoolean(userKey(KEY_CARRY_OVER_ENABLED, accountId), false)
    
    /** Sets carry-over status for an account. */
    fun setCarryOverEnabled(accountId: Long, enabled: Boolean) = 
        prefs.edit().putBoolean(userKey(KEY_CARRY_OVER_ENABLED, accountId), enabled).apply()

    /** Checks if only positive balances should be carried over. */
    fun isCarryOverPositiveOnly(accountId: Long): Boolean = 
        prefs.getBoolean(userKey(KEY_CARRY_OVER_POSITIVE_ONLY, accountId), false)
    
    /** Sets whether only positive balances are carried over. */
    fun setCarryOverPositiveOnly(accountId: Long, enabled: Boolean) = 
        prefs.edit().putBoolean(userKey(KEY_CARRY_OVER_POSITIVE_ONLY, accountId), enabled).apply()

    /** Checks if carry-over should be added to the display of total income. */
    fun isCarryOverAddToIncome(accountId: Long): Boolean = 
        prefs.getBoolean(userKey(KEY_CARRY_OVER_ADD_TO_INCOME, accountId), false)
    
    /** Sets whether carry-over is added to total income display. */
    fun setCarryOverAddToIncome(accountId: Long, enabled: Boolean) = 
        prefs.edit().putBoolean(userKey(KEY_CARRY_OVER_ADD_TO_INCOME, accountId), enabled).apply()

    // --- General Settings ---

    /** Retrieves the frequency of reminders. */
    fun getReminderFrequency(): String = prefs.getString(KEY_REMINDER_FREQUENCY, "Every Day") ?: "Every Day"
    
    /** Sets the frequency of reminders. */
    fun setReminderFrequency(frequency: String) = prefs.edit().putString(KEY_REMINDER_FREQUENCY, frequency).apply()

    /** Gets the configured reminder time as a Pair of (Hour, Minute). */
    fun getReminderTime(): Pair<Int, Int> = Pair(prefs.getInt(KEY_REMINDER_HOUR, 20), prefs.getInt(KEY_REMINDER_MINUTE, 0))
    
    /** Sets the reminder time. */
    fun setReminderTime(hour: Int, minute: Int) = prefs.edit().putInt(KEY_REMINDER_HOUR, hour).putInt(KEY_REMINDER_MINUTE, minute).apply()

    /** Retrieves the current application language preference. */
    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "System") ?: "System"
    
    /** Sets the application language preference. */
    fun setLanguage(language: String) = prefs.edit().putString(KEY_LANGUAGE, language).apply()
}
