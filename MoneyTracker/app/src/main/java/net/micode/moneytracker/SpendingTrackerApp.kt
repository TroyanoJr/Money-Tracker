package net.micode.moneytracker

import android.app.Application
import com.google.android.gms.ads.MobileAds
import net.micode.moneytracker.data.AppDatabase
import net.micode.moneytracker.data.DatabaseProvider
import net.micode.moneytracker.util.AppLifecycleTracker
import net.sqlcipher.database.SQLiteDatabase

/**
 * Custom Application class for Spending Tracker.
 * Refactored to comply with SOLID principles by delegating infrastructure 
 * concerns (Persistence and Lifecycle tracking) to specialized components.
 */
class SpendingTrackerApp : Application() {

    /**
     * Tracker for monitoring whether the application is in the foreground.
     */
    private val lifecycleTracker = AppLifecycleTracker()

    /**
     * The singleton instance of the Room database, initialized via [DatabaseProvider].
     */
    lateinit var database: AppDatabase
        private set

    companion object {
        private lateinit var instance: SpendingTrackerApp

        /**
         * Global access to the app's foreground state.
         * Delegated to [AppLifecycleTracker] for better separation of concerns.
         */
        val isAppInForeground: Boolean
            get() = instance.lifecycleTracker.isAppInForeground
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Register the specialized lifecycle tracker
        registerActivityLifecycleCallbacks(lifecycleTracker)
        
        // Initialize core SDKs
        MobileAds.initialize(this) {}

        // Load SQLCipher native libraries required for encrypted database
        SQLiteDatabase.loadLibs(this)
        
        // Initialize the encrypted database using the provider (DIP)
        database = DatabaseProvider(this).provideDatabase()
    }
}
