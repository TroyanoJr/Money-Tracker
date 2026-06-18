package net.micode.moneytracker.util

import android.content.Context
import android.net.Uri
import android.util.Log
import net.micode.moneytracker.data.AppDatabase
import java.io.File

/**
 * Orchestrates atomic backup and restore operations by coordinating specialized services.
 * Refactored to comply with SOLID principles (SRP, OCP, DIP).
 */
object BackupManager {
    private const val TAG = "BackupManager"
    private const val DB_NAME = "spending-tracker-db"
    private const val PREFS_MAIN = "app_settings.xml"
    private const val PREFS_SECURITY = "security_prefs.xml"

    private val zipService = ZipService()

    /**
     * Creates a full backup of the database and app settings.
     */
    fun createBackup(context: Context, database: AppDatabase, targetUri: Uri): Boolean {
        return try {
            val sources = listOf(
                DatabaseBackupSource(context, database, DB_NAME),
                PreferencesBackupSource(context, listOf(PREFS_MAIN, PREFS_SECURITY))
            )

            val filesToBackup = sources.flatMap { it.getBackupFiles() }

            context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                zipService.zipFiles(filesToBackup, outputStream)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            false
        }
    }

    /**
     * Restores the application state from a ZIP backup.
     */
    fun restoreBackup(context: Context, sourceUri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DB_NAME)
            val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")

            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                zipService.unzipFiles(inputStream) { entryName ->
                    when (entryName) {
                        DB_NAME -> {
                            // Clean up WAL/SHM files for consistency
                            File(dbFile.path + "-wal").delete()
                            File(dbFile.path + "-shm").delete()
                            dbFile
                        }
                        PREFS_MAIN -> File(sharedPrefsDir, PREFS_MAIN)
                        PREFS_SECURITY -> File(sharedPrefsDir, PREFS_SECURITY)
                        else -> null
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            false
        }
    }
}
