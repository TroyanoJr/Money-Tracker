package net.micode.moneytracker.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import net.micode.moneytracker.util.DbPassphraseManager
import net.micode.moneytracker.util.SettingsManager
import net.sqlcipher.database.SupportFactory

/**
 * Responsible for creating, configuring, and recovering the encrypted Room database.
 * Encapsulates security and initialization logic (SRP).
 */
class DatabaseProvider(private val context: Context) {

    companion object {
        private const val TAG = "DatabaseProvider"
        private const val DB_NAME = "spending-tracker-db"
        private const val SECURITY_PREFS = "security_prefs"
        private const val KEY_DB_ENCRYPTED_READY = "db_encrypted_ready_v1"
    }

    /**
     * Entry point to initialize the encrypted database with automatic recovery.
     */
    fun provideDatabase(): AppDatabase {
        resetLegacyPlainDatabaseIfNeeded()
        return createEncryptedDatabaseWithRecovery()
    }

    private fun resetLegacyPlainDatabaseIfNeeded() {
        val securityPrefs = context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE)
        if (securityPrefs.getBoolean(KEY_DB_ENCRYPTED_READY, false)) return
        
        val dbFile = context.getDatabasePath(DB_NAME)
        if (dbFile.exists()) {
            // Optional: Migration or cleanup logic for unencrypted legacy DB
        }
    }

    private fun createEncryptedDatabaseWithRecovery(): AppDatabase {
        return try {
            buildEncryptedDatabase()
        } catch (error: Exception) {
            Log.e(TAG, "Encrypted DB init failed. Attempting recovery.", error)
            try {
                buildEncryptedDatabase()
            } catch (e: Exception) {
                Log.e(TAG, "Final fallback: resetting storage.")
                resetEncryptedStorage()
                buildEncryptedDatabase()
            }
        }
    }

    private fun buildEncryptedDatabase(): AppDatabase {
        val passphrase = DbPassphraseManager.getOrCreatePassphrase(context)
        val database = Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .addMigrations(
                AppDatabase.MIGRATION_4_5, 
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8
            )
            .openHelperFactory(SupportFactory(passphrase))
            .build()
        
        markEncryptedDatabaseReady()
        return database
    }

    private fun resetEncryptedStorage() {
        DbPassphraseManager.clearStoredPassphrase(context)
        context.deleteDatabase(DB_NAME)
        context.deleteDatabase("$DB_NAME-wal")
        context.deleteDatabase("$DB_NAME-shm")
        context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_DB_ENCRYPTED_READY, false).apply()
        SettingsManager(context).setHasSeededCategories(false)
    }

    private fun markEncryptedDatabaseReady() {
        context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_DB_ENCRYPTED_READY, true).apply()
    }
}
