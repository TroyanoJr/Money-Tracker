package net.micode.spendingtracker.util

import android.content.Context
import android.util.Log
import net.micode.spendingtracker.data.AppDatabase
import java.io.File

/**
 * Backup source implementation for the Room database.
 * Handles the WAL checkpoint to ensure data consistency.
 */
class DatabaseBackupSource(
    private val context: Context,
    private val database: AppDatabase,
    private val dbName: String
) : BackupSource {

    override fun getBackupFiles(): List<Pair<File, String>> {
        return try {
            // Force checkpoint to merge WAL/SHM into main DB file
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
                if (cursor.moveToFirst()) {
                    Log.d("DatabaseBackupSource", "Checkpoint status: ${cursor.getInt(0)}")
                }
            }
            val dbFile = context.getDatabasePath(dbName)
            if (dbFile.exists()) listOf(dbFile to dbName) else emptyList()
        } catch (e: Exception) {
            Log.e("DatabaseBackupSource", "Failed to prepare database for backup", e)
            emptyList()
        }
    }
}
