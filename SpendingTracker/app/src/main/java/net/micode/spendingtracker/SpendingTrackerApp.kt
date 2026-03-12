package net.micode.spendingtracker

import android.app.Application
import androidx.room.Room
import net.micode.spendingtracker.data.AppDatabase

class SpendingTrackerApp : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "spending-tracker-db"
        )
        .fallbackToDestructiveMigration() // Evita crashes al cambiar el modelo de datos
        .build()
    }
}
