package net.micode.spendingtracker

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.room.Room
import net.micode.spendingtracker.BuildConfig
import net.micode.spendingtracker.data.AppDatabase
import net.micode.spendingtracker.util.DbPassphraseManager
import net.micode.spendingtracker.util.SettingsManager
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

/**
 * Custom Application class.
 * Configured for production stability and data integrity.
 */
class SpendingTrackerApp : Application() {
    lateinit var database: AppDatabase
        private set

    private companion object {
        const val TAG = "SpendingTrackerApp"
        const val DB_NAME = "spending-tracker-db"
        const val SECURITY_PREFS = "security_prefs"
        const val KEY_DB_ENCRYPTED_READY = "db_encrypted_ready_v1"
    }

    override fun onCreate() {
        super.onCreate()
        SQLiteDatabase.loadLibs(this)
        resetLegacyPlainDatabaseIfNeeded()
        database = createEncryptedDatabaseWithRecovery()
    }

    private fun resetLegacyPlainDatabaseIfNeeded() {
        val securityPrefs = getSharedPreferences(SECURITY_PREFS, MODE_PRIVATE)
        if (securityPrefs.getBoolean(KEY_DB_ENCRYPTED_READY, false)) return

        deleteDatabase(DB_NAME)
        deleteDatabase("${DB_NAME}-wal")
        deleteDatabase("${DB_NAME}-shm")
        SettingsManager(this).setHasSeededCategories(false)
    }

    private fun createEncryptedDatabaseWithRecovery(): AppDatabase {
        return runCatching { buildEncryptedDatabase() }
            .getOrElse { error ->
                Log.e(TAG, "Encrypted DB init failed. Resetting storage and retrying.", error)
                resetEncryptedStorage()
                if (BuildConfig.DEBUG) {
                    Toast.makeText(
                        this,
                        "DB encryption recovery applied (beta).",
                        Toast.LENGTH_LONG
                    ).show()
                }
                buildEncryptedDatabase()
            }
    }

    private fun buildEncryptedDatabase(): AppDatabase {
        val passphrase = DbPassphraseManager.getOrCreatePassphrase(this)
        val database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            DB_NAME
        )
            .addMigrations(AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6)
            .openHelperFactory(SupportFactory(passphrase))
            .build()

        // Force opening now so initialization failures are handled in one place.
        database.openHelper.writableDatabase
        markEncryptedDatabaseReady()
        return database
    }

    private fun resetEncryptedStorage() {
        DbPassphraseManager.clearStoredPassphrase(this)
        deleteDatabase(DB_NAME)
        deleteDatabase("${DB_NAME}-wal")
        deleteDatabase("${DB_NAME}-shm")
        getSharedPreferences(SECURITY_PREFS, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DB_ENCRYPTED_READY, false)
            .apply()
        SettingsManager(this).setHasSeededCategories(false)
    }

    private fun markEncryptedDatabaseReady() {
        getSharedPreferences(SECURITY_PREFS, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DB_ENCRYPTED_READY, true)
            .apply()
    }
}
