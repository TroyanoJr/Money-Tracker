package net.micode.moneytracker.util

import android.content.SharedPreferences

/**
 * Manages security-related preferences such as passcodes and biometric settings.
 * Part of the SettingsManager refactor to comply with SRP.
 */
class SecuritySettings(private val prefs: SharedPreferences) {

    companion object {
        const val KEY_PASSCODE_ENABLED = "passcode_enabled"
        const val KEY_FACE_UNLOCK_ENABLED = "face_unlock_enabled"
        const val KEY_FINGERPRINT_ENABLED = "fingerprint_enabled"
        const val KEY_LOCK_TYPE = "lock_type"
        const val KEY_PASSCODE_VALUE = "passcode_value"
    }

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
}
