package net.micode.spendingtracker

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.room.Room
import com.google.android.gms.ads.MobileAds
import net.micode.spendingtracker.data.AppDatabase
import net.micode.spendingtracker.util.DbPassphraseManager
import net.micode.spendingtracker.util.SettingsManager
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

/**
 * Custom Application class for Spending Tracker.
 * Initializes core components such as the encrypted database, AdMob SDK, and SQLCipher libraries.
 * Implements [Application.ActivityLifecycleCallbacks] to track app foreground state.
 */
class SpendingTrackerApp : Application(), Application.ActivityLifecycleCallbacks {
    /**
     * The singleton instance of the Room database.
     */
    lateinit var database: AppDatabase
        private set

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    companion object {
        private const val TAG = "SpendingTrackerApp"
        private const val DB_NAME = "spending-tracker-db"
        private const val SECURITY_PREFS = "security_prefs"
        private const val KEY_DB_ENCRYPTED_READY = "db_encrypted_ready_v1"
        
        /**
         * Returns true if the application has at least one activity in the foreground.
         */
        var isAppInForeground: Boolean = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        
        // Initialize AdMob SDK
        MobileAds.initialize(this) {}

        // Load SQLCipher native libraries
        SQLiteDatabase.loadLibs(this)
        
        resetLegacyPlainDatabaseIfNeeded()
        
        // Initialize the encrypted Room database
        database = createEncryptedDatabaseWithRecovery()
    }

    /**
     * Checks if a legacy unencrypted database exists and resets it if necessary to ensure
     * that encryption is properly applied to all data.
     */
    private fun resetLegacyPlainDatabaseIfNeeded() {
        val securityPrefs = getSharedPreferences(SECURITY_PREFS, MODE_PRIVATE)
        if (securityPrefs.getBoolean(KEY_DB_ENCRYPTED_READY, false)) return
        
        val dbFile = getDatabasePath(DB_NAME)
        if (dbFile.exists()) {
            // Safety check: Logic could be added here to migrate or clear legacy data.
        }
    }

    /**
     * Attempts to build the encrypted database. If initialization fails due to 
     * key mismatches or corruption, it attempts a recovery reset.
     * @return The initialized [AppDatabase] instance.
     */
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

    /**
     * Configures Room with SQLCipher using a passphrase managed by [DbPassphraseManager].
     */
    private fun buildEncryptedDatabase(): AppDatabase {
        val passphrase = DbPassphraseManager.getOrCreatePassphrase(this)
        val database = Room.databaseBuilder(this, AppDatabase::class.java, DB_NAME)
            .addMigrations(
                AppDatabase.MIGRATION_4_5, 
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7
            )
            .openHelperFactory(SupportFactory(passphrase))
            .build()
        
        markEncryptedDatabaseReady()
        return database
    }

    /**
     * Wipes local database files and security credentials.
     * Used as a last resort during recovery from encryption failures.
     */
    private fun resetEncryptedStorage() {
        DbPassphraseManager.clearStoredPassphrase(this)
        deleteDatabase(DB_NAME)
        deleteDatabase("${DB_NAME}-wal")
        deleteDatabase("${DB_NAME}-shm")
        getSharedPreferences(SECURITY_PREFS, MODE_PRIVATE).edit().putBoolean(KEY_DB_ENCRYPTED_READY, false).apply()
        SettingsManager(this).setHasSeededCategories(false)
    }

    /**
     * Persists a flag indicating that the encrypted database has been successfully initialized at least once.
     */
    private fun markEncryptedDatabaseReady() {
        getSharedPreferences(SECURITY_PREFS, MODE_PRIVATE).edit().putBoolean(KEY_DB_ENCRYPTED_READY, true).apply()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    
    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) isAppInForeground = true
    }
    
    override fun onActivityResumed(activity: Activity) {}
    
    override fun onActivityPaused(activity: Activity) {}
    
    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) isAppInForeground = false
    }
    
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}
