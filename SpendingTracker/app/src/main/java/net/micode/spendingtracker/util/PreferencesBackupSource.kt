package net.micode.spendingtracker.util

import android.content.Context
import java.io.File

/**
 * Backup source implementation for application shared preferences.
 */
class PreferencesBackupSource(
    private val context: Context,
    private val preferenceNames: List<String>
) : BackupSource {

    override fun getBackupFiles(): List<Pair<File, String>> {
        val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        return preferenceNames.mapNotNull { name ->
            val file = File(sharedPrefsDir, name)
            if (file.exists()) file to name else null
        }
    }
}
