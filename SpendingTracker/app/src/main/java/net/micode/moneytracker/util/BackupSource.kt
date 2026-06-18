package net.micode.moneytracker.util

import java.io.File

/**
 * Interface representing a source of data for the backup system.
 * This allows the backup system to be extensible (OCP).
 */
interface BackupSource {
    /**
     * Returns a list of files that should be included in the backup,
     * along with their intended entry names in the archive.
     */
    fun getBackupFiles(): List<Pair<File, String>>
}
