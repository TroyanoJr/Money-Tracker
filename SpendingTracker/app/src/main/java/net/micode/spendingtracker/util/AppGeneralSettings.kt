package net.micode.spendingtracker.util

import android.content.SharedPreferences

/**
 * Manages general application settings such as premium status, language, 
 * and data seeding status.
 * Part of the SettingsManager refactor to comply with SRP.
 */
class AppGeneralSettings(private val prefs: SharedPreferences) {

    companion object {
        const val KEY_HAS_SEEDED_CATEGORIES = "has_seeded_categories"
        const val KEY_IS_PREMIUM = "is_premium_user"
        const val KEY_LANGUAGE = "language_pref"
    }

    /** Returns true if the user has premium status. */
    fun isPremium(): Boolean = prefs.getBoolean(KEY_IS_PREMIUM, false)
    
    /** Sets the premium status for the user. */
    fun setPremiumStatus(isPremium: Boolean) = prefs.edit().putBoolean(KEY_IS_PREMIUM, isPremium).apply()

    /** Marks whether default categories have been seeded into the database. */
    fun setHasSeededCategories(hasSeeded: Boolean) = prefs.edit().putBoolean(KEY_HAS_SEEDED_CATEGORIES, hasSeeded).apply()
    
    /** Checks if default categories have been seeded. */
    fun hasSeededCategories(): Boolean = prefs.getBoolean(KEY_HAS_SEEDED_CATEGORIES, false)

    /** Retrieves the current application language preference. */
    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "System") ?: "System"
    
    /** Sets the application language preference. */
    fun setLanguage(language: String) = prefs.edit().putString(KEY_LANGUAGE, language).apply()
}
