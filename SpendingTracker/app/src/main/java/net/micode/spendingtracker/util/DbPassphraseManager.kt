package net.micode.spendingtracker.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object DbPassphraseManager {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "spending_tracker_db_key_v1"
    private const val PREFS_NAME = "security_prefs"
    private const val KEY_ENCRYPTED_DB_PASSPHRASE = "encrypted_db_passphrase_v1"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    fun getOrCreatePassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedValue = prefs.getString(KEY_ENCRYPTED_DB_PASSPHRASE, null)

        if (!storedValue.isNullOrEmpty()) {
            return decrypt(Base64.decode(storedValue, Base64.NO_WRAP))
        }

        val passphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val encrypted = encrypt(passphrase)
        prefs.edit()
            .putString(KEY_ENCRYPTED_DB_PASSPHRASE, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .apply()
        return passphrase
    }

    fun clearStoredPassphrase(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_ENCRYPTED_DB_PASSPHRASE)
            .apply()
    }

    private fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plain)
        return iv + encrypted
    }

    private fun decrypt(payload: ByteArray): ByteArray {
        require(payload.size > GCM_IV_LENGTH) { "Invalid encrypted payload" }
        val iv = payload.copyOfRange(0, GCM_IV_LENGTH)
        val encrypted = payload.copyOfRange(GCM_IV_LENGTH, payload.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)
        return cipher.doFinal(encrypted)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val existing = runCatching {
            keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        }.getOrNull()
        if (existing != null) return existing.secretKey

        if (keyStore.containsAlias(KEY_ALIAS)) {
            runCatching { keyStore.deleteEntry(KEY_ALIAS) }
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}
