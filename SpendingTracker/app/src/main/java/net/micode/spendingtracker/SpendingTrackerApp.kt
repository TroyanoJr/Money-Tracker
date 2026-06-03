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

class SpendingTrackerApp : Application(), Application.ActivityLifecycleCallbacks {
    lateinit var database: AppDatabase
        private set

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    companion object {
        private const val TAG = "SpendingTrackerApp"
        private const val DB_NAME = "spending-tracker-db"
        private const val SECURITY_PREFS = "security_prefs"
        private const val KEY_DB_ENCRYPTED_READY = "db_encrypted_ready_v1"
        var isAppInForeground: Boolean = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        
        // Initialize AdMob SDK
        MobileAds.initialize(this) {}

        SQLiteDatabase.loadLibs(this)
        
        resetLegacyPlainDatabaseIfNeeded()
        
        database = createEncryptedDatabaseWithRecovery()
    }

    private fun resetLegacyPlainDatabaseIfNeeded() {
        val securityPrefs = getSharedPreferences(SECURITY_PREFS, MODE_PRIVATE)
        if (securityPrefs.getBoolean(KEY_DB_ENCRYPTED_READY, false)) return
        
        val dbFile = getDatabasePath(DB_NAME)
        if (dbFile.exists()) {
            // Safety check
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
        val passphrase = DbPassphraseManager.getOrCreatePassphrase(this)
        val database = Room.databaseBuilder(this, AppDatabase::class.java, DB_NAME)
            .addMigrations(
                AppDatabase.MIGRATION_4_5, 
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7
            )
            .openHelperFactory(SupportFactory(passphrase))
            .build()
        
        // Removed forced writableDatabase access on the main thread to improve startup performance.
        // Room will initialize the database lazily when first accessed from the ViewModel/Repository.
        markEncryptedDatabaseReady()
        return database
    }

    private fun resetEncryptedStorage() {
        DbPassphraseManager.clearStoredPassphrase(this)
        deleteDatabase(DB_NAME)
        deleteDatabase("${DB_NAME}-wal")
        deleteDatabase("${DB_NAME}-shm")
        getSharedPreferences(SECURITY_PREFS, MODE_PRIVATE).edit().putBoolean(KEY_DB_ENCRYPTED_READY, false).apply()
        SettingsManager(this).setHasSeededCategories(false)
    }

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
