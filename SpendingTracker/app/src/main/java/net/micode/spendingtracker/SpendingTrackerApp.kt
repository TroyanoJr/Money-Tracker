package net.micode.spendingtracker

import android.app.Application
import androidx.room.Room
import net.micode.spendingtracker.data.AppDatabase

/**
 * Custom Application class.
 * Configured for production stability and data integrity.
 */
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
        // Eliminado: fallbackToDestructiveMigration() para proteger los datos del usuario.
        // A partir de ahora, los cambios en el esquema requerirán objetos Migration específicos.
        .build()
    }
}
