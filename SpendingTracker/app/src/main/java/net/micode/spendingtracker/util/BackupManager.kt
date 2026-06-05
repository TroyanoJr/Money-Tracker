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
 * Utility object for managing atomic backup and restore operations.
 * It packages the database and shared preference files into a single ZIP archive.
 */
object BackupManager {
    private const val TAG = "BackupManager"
    private const val DB_NAME = "spending-tracker-db"
    private const val PREFS_MAIN = "app_settings.xml"
    private const val PREFS_SECURITY = "security_prefs.xml"

    /**
     * Bundles the database and application settings into a ZIP file at the specified URI.
     * @param context Application context.
     * @param database The [AppDatabase] instance to be backed up.
     * @param targetUri The destination URI for the backup file.
     * @return True if the backup was successful, false otherwise.
     */
    fun createBackup(context: Context, database: AppDatabase, targetUri: Uri): Boolean {
        return try {
            // 1. Force database checkpoint to merge WAL/SHM files into the main DB file
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
                if (cursor.moveToFirst()) Log.d(TAG, "Checkpoint status: ${cursor.getInt(0)}")
            }

            val dbFile = context.getDatabasePath(DB_NAME)
            val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")

            context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                    // Add Database file to ZIP
                    if (dbFile.exists()) addToZip(dbFile, DB_NAME, zipOut)
                    
                    // Add relevant XML preference files to ZIP
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
     * Extracts files from a ZIP backup and restores them to the application's internal storage.
     * Deletes existing WAL/SHM files to ensure database consistency after restore.
     * @param context Application context.
     * @param sourceUri The source URI of the backup file.
     * @return True if restore was successful, false otherwise.
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

    /**
     * Internal helper to add a single file to a [ZipOutputStream].
     */
    private fun addToZip(file: File, entryName: String, zipOut: ZipOutputStream) {
        FileInputStream(file).use { input ->
            val entry = ZipEntry(entryName)
            zipOut.putNextEntry(entry)
            input.copyTo(zipOut)
            zipOut.closeEntry()
        }
    }
}
