package net.micode.spendingtracker.util

import android.content.Context
import android.net.Uri
import android.util.Log
import net.micode.spendingtracker.data.AppDatabase
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Manages atomic backup and restore operations using ZIP packaging.
 */
object BackupManager {
    private const val TAG = "BackupManager"
    private const val DB_NAME = "spending-tracker-db"
    private const val PREFS_MAIN = "app_settings.xml"
    private const val PREFS_SECURITY = "security_prefs.xml"

    /**
     * Bundles the database and all settings into a single ZIP file at targetUri.
     */
    fun createBackup(context: Context, database: AppDatabase, targetUri: Uri): Boolean {
        return try {
            // 1. Force database checkpoint to merge WAL files into the main DB file
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
                if (cursor.moveToFirst()) Log.d(TAG, "Checkpoint status: ${cursor.getInt(0)}")
            }

            val dbFile = context.getDatabasePath(DB_NAME)
            val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")

            context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                    // Add Database
                    if (dbFile.exists()) addToZip(dbFile, DB_NAME, zipOut)
                    
                    // Add all relevant XML preferences
                    File(sharedPrefsDir, PREFS_MAIN).let { if (it.exists()) addToZip(it, PREFS_MAIN, zipOut) }
                    File(sharedPrefsDir, PREFS_SECURITY).let { if (it.exists()) addToZip(it, PREFS_SECURITY, zipOut) }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            false
        }
    }

    /**
     * Extracts files from ZIP sourceUri and restores them to internal storage.
     */
    fun restoreBackup(context: Context, sourceUri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DB_NAME)
            val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")

            sharedPrefsDir.mkdirs()

            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                    var entry: ZipEntry? = zipIn.nextEntry
                    while (entry != null) {
                        val outFile = when (entry.name) {
                            DB_NAME -> {
                                // Important: Delete existing WAL/SHM to avoid schema conflicts with restored DB
                                File(dbFile.path + "-wal").delete()
                                File(dbFile.path + "-shm").delete()
                                dbFile
                            }
                            PREFS_MAIN -> File(sharedPrefsDir, PREFS_MAIN)
                            PREFS_SECURITY -> File(sharedPrefsDir, PREFS_SECURITY)
                            else -> null
                        }

                        outFile?.let {
                            it.parentFile?.mkdirs()
                            FileOutputStream(it).use { out -> zipIn.copyTo(out) }
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            false
        }
    }

    private fun addToZip(file: File, entryName: String, zipOut: ZipOutputStream) {
        FileInputStream(file).use { input ->
            val entry = ZipEntry(entryName)
            zipOut.putNextEntry(entry)
            input.copyTo(zipOut)
            zipOut.closeEntry()
        }
    }
}
