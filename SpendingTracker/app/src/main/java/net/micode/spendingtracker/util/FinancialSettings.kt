package net.micode.spendingtracker.util

import android.content.SharedPreferences

/**
 * Manages financial preferences including currency codes and account-specific budget configurations.
 * Part of the SettingsManager refactor to comply with SRP.
 */
class FinancialSettings(private val prefs: SharedPreferences) {

    companion object {
        const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        const val KEY_CURRENCY_CODE = "currency_code"
        const val KEY_BUDGET_MODE_ENABLED = "budget_mode_enabled"
        const val KEY_MONTHLY_BUDGET_AMOUNT = "monthly_budget_amount"
        const val KEY_INCLUDE_INCOME_IN_BUDGET = "include_income_in_budget"
        const val KEY_CARRY_OVER_ENABLED = "carry_over_enabled"
        const val KEY_CARRY_OVER_POSITIVE_ONLY = "carry_over_positive_only"
        const val KEY_CARRY_OVER_ADD_TO_INCOME = "carry_over_add_to_income"
        
        // New keys for time period configuration
        const val KEY_DEFAULT_TIME_PERIOD = "default_time_period"
        const val KEY_MONTH_START_DAY = "month_start_day"
        const val KEY_WEEK_START_DAY = "week_start_day"
    }

    /** Generates a unique preference key for account-specific settings. */
    private fun userKey(key: String, accountId: Long): String = "${accountId}_$key"

    /** Sets the currency symbol (e.g., "$", "€"). */
    fun setCurrencySymbol(symbol: String) = prefs.edit().putString(KEY_CURRENCY_SYMBOL, symbol).apply()
    
    /** Retrieves the current currency symbol. Defaults to "¥". */
    fun getCurrencySymbol(): String = prefs.getString(KEY_CURRENCY_SYMBOL, "¥") ?: "¥"

    /** Sets the ISO currency code (e.g., "USD"). */
    fun setCurrencyCode(code: String) = prefs.edit().putString(KEY_CURRENCY_CODE, code).apply()
    
    /** Retrieves the current currency code. Defaults to "CNY". */
    fun getCurrencyCode(): String = prefs.getString(KEY_CURRENCY_CODE, "CNY") ?: "CNY"

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

    /** Gets the default time period (DAILY, WEEKLY, MONTHLY, YEARLY). Defaults to MONTHLY. */
    fun getDefaultTimePeriod(): String = 
        prefs.getString(KEY_DEFAULT_TIME_PERIOD, "MONTH") ?: "MONTH"

    /** Sets the default time period. */
    fun setDefaultTimePeriod(period: String) = 
        prefs.edit().putString(KEY_DEFAULT_TIME_PERIOD, period).apply()

    /** Gets the day of the month when the period starts (1-31). Defaults to 1. */
    fun getMonthStartDay(): Int = 
        prefs.getInt(KEY_MONTH_START_DAY, 1)

    /** Sets the month start day. */
    fun setMonthStartDay(day: Int) = 
        prefs.edit().putInt(KEY_MONTH_START_DAY, day).apply()

    /** Gets the day of the week when the period starts (Calendar.SUNDAY, etc). Defaults to Calendar.MONDAY (2). */
    fun getWeekStartDay(): Int = 
        prefs.getInt(KEY_WEEK_START_DAY, 2)

    /** Sets the week start day. */
    fun setWeekStartDay(day: Int) = 
        prefs.edit().putInt(KEY_WEEK_START_DAY, day).apply()
}
