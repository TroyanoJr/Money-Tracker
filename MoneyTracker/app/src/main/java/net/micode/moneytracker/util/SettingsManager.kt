package net.micode.moneytracker.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Main entry point for application settings. 
 * Refactored using the Facade pattern to comply with SOLID (SRP). 
 * It delegates specific responsibilities to specialized classes.
 */
class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // Specialized delegates
    private val security = SecuritySettings(prefs)
    private val financial = FinancialSettings(prefs)
    private val notifications = NotificationSettings(prefs)
    private val general = AppGeneralSettings(prefs)

    // --- Security Settings (Delegated) ---
    fun isPasscodeEnabled(): Boolean = security.isPasscodeEnabled()
    fun setPasscodeEnabled(enabled: Boolean) = security.setPasscodeEnabled(enabled)
    fun isFaceUnlockEnabled(): Boolean = security.isFaceUnlockEnabled()
    fun setFaceUnlockEnabled(enabled: Boolean) = security.setFaceUnlockEnabled(enabled)
    fun isFingerprintEnabled(): Boolean = security.isFingerprintEnabled()
    fun setFingerprintEnabled(enabled: Boolean) = security.setFingerprintEnabled(enabled)
    fun getLockType(): String = security.getLockType()
    fun setLockType(type: String) = security.setLockType(type)
    fun getPasscode(): String = security.getPasscode()
    fun setPasscode(passcode: String) = security.setPasscode(passcode)

    // --- Financial & Budget Settings (Delegated) ---
    fun setCurrencySymbol(symbol: String) = financial.setCurrencySymbol(symbol)
    fun getCurrencySymbol(): String = financial.getCurrencySymbol()
    fun setCurrencyCode(code: String) = financial.setCurrencyCode(code)
    fun getCurrencyCode(): String = financial.getCurrencyCode()
    
    fun isBudgetModeEnabled(accountId: Long): Boolean = financial.isBudgetModeEnabled(accountId)
    fun setBudgetModeEnabled(accountId: Long, enabled: Boolean) = financial.setBudgetModeEnabled(accountId, enabled)
    fun getMonthlyBudget(accountId: Long): Double = financial.getMonthlyBudget(accountId)
    fun setMonthlyBudget(accountId: Long, amount: Double) = financial.setMonthlyBudget(accountId, amount)
    fun isIncludeIncomeInBudget(accountId: Long): Boolean = financial.isIncludeIncomeInBudget(accountId)
    fun setIncludeIncomeInBudget(accountId: Long, enabled: Boolean) = financial.setIncludeIncomeInBudget(accountId, enabled)
    
    fun isCarryOverEnabled(accountId: Long): Boolean = financial.isCarryOverEnabled(accountId)
    fun setCarryOverEnabled(accountId: Long, enabled: Boolean) = financial.setCarryOverEnabled(accountId, enabled)
    fun isCarryOverPositiveOnly(accountId: Long): Boolean = financial.isCarryOverPositiveOnly(accountId)
    fun setCarryOverPositiveOnly(accountId: Long, enabled: Boolean) = financial.setCarryOverPositiveOnly(accountId, enabled)
    fun isCarryOverAddToIncome(accountId: Long): Boolean = financial.isCarryOverAddToIncome(accountId)
    fun setCarryOverAddToIncome(accountId: Long, enabled: Boolean) = financial.setCarryOverAddToIncome(accountId, enabled)

    // --- Time Period Settings (Delegated to financial) ---
    fun getDefaultTimePeriod(): String = financial.getDefaultTimePeriod()
    fun setDefaultTimePeriod(period: String) = financial.setDefaultTimePeriod(period)
    fun getMonthStartDay(): Int = financial.getMonthStartDay()
    fun setMonthStartDay(day: Int) = financial.setMonthStartDay(day)
    fun getWeekStartDay(): Int = financial.getWeekStartDay()
    fun setWeekStartDay(day: Int) = financial.setWeekStartDay(day)

    // --- Notification & Reminder Settings (Delegated) ---
    fun getReminderFrequency(): String = notifications.getReminderFrequency()
    fun setReminderFrequency(frequency: String) = notifications.setReminderFrequency(frequency)
    fun getReminderTime(): Pair<Int, Int> = notifications.getReminderTime()
    fun setReminderTime(hour: Int, minute: Int) = notifications.setReminderTime(hour, minute)

    // --- App General Settings (Delegated) ---
    fun isPremium(): Boolean = general.isPremium()
    fun setPremiumStatus(isPremium: Boolean) = general.setPremiumStatus(isPremium)
    fun setHasSeededCategories(hasSeeded: Boolean) = general.setHasSeededCategories(hasSeeded)
    fun hasSeededCategories(): Boolean = general.hasSeededCategories()
    fun getLanguage(): String = general.getLanguage()
    fun setLanguage(language: String) = general.setLanguage(language)
}
