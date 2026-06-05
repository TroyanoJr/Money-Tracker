package net.micode.spendingtracker.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages the generation, migration, and retrieval of the database passphrase.
 * Supports migrating legacy Android KeyStore encrypted keys to a more portable format
 * while ensuring data security.
 */
object DbPassphraseManager {
    private const val TAG = "DbPassphraseManager"
    private const val OLD_PREFS = "security_prefs"
    private const val OLD_KEY_ALIAS = "spending_tracker_db_key_v1"
    private const val OLD_KEY_PASSPHRASE = "encrypted_db_passphrase_v1"
    
    private const val NEW_PREFS = "app_settings"
    private const val NEW_KEY_PASSPHRASE = "db_passphrase_portable_v2"

    /**
     * Retrieves the current database passphrase or creates a new one if it doesn't exist.
     * Handles migration from legacy encrypted preferences to the new portable format.
     * @param context Application context.
     * @return The 32-byte passphrase as a [ByteArray].
     */
    fun getOrCreatePassphrase(context: Context): ByteArray {
        val newPrefs = context.getSharedPreferences(NEW_PREFS, Context.MODE_PRIVATE)
        val portableValue = newPrefs.getString(NEW_KEY_PASSPHRASE, null)

        // 1. Return existing portable key if available
        if (!portableValue.isNullOrEmpty()) {
            return Base64.decode(portableValue, Base64.NO_WRAP)
        }

        // 2. Try to migrate from old Keystore-encrypted key
        val oldPrefs = context.getSharedPreferences(OLD_PREFS, Context.MODE_PRIVATE)
        val oldEncryptedValue = oldPrefs.getString(OLD_KEY_PASSPHRASE, null)
        
        if (!oldEncryptedValue.isNullOrEmpty()) {
            try {
                val decrypted = decryptOldKey(Base64.decode(oldEncryptedValue, Base64.NO_WRAP))
                savePortableKey(newPrefs, decrypted)
                return decrypted
            } catch (e: Exception) {
                Log.e(TAG, "Migration failed", e)
            }
        }

        // 3. Generate a brand new key if nothing else exists
        val passphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
        savePortableKey(newPrefs, passphrase)
        return passphrase
    }

    /**
     * Saves the passphrase in a Base64 encoded format in SharedPreferences.
     */
    private fun savePortableKey(prefs: android.content.SharedPreferences, key: ByteArray) {
        prefs.edit()
            .putString(NEW_KEY_PASSPHRASE, Base64.encodeToString(key, Base64.NO_WRAP))
            .apply()
    }

    /**
     * Decrypts a legacy key that was protected using the Android KeyStore.
     */
    private fun decryptOldKey(payload: ByteArray): ByteArray {
        val iv = payload.copyOfRange(0, 12)
        val encrypted = payload.copyOfRange(12, payload.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val secretKey = (keyStore.getEntry(OLD_KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(encrypted)
    }

    /**
     * Clears all stored passphrases from both legacy and new preferences.
     * Typically used during a factory reset of the application data.
     */
    fun clearStoredPassphrase(context: Context) {
        context.getSharedPreferences(NEW_PREFS, Context.MODE_PRIVATE).edit().remove(NEW_KEY_PASSPHRASE).apply()
        context.getSharedPreferences(OLD_PREFS, Context.MODE_PRIVATE).edit().remove(OLD_KEY_PASSPHRASE).apply()
    }
}
