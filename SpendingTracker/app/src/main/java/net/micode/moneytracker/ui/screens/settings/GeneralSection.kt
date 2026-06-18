package net.micode.moneytracker.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import net.micode.moneytracker.util.SettingsManager

/**
 * Section for General settings like Reminders and Language.
 */
@Composable
fun GeneralSection(
    settingsManager: SettingsManager,
    onNavigateToReminders: () -> Unit
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    // We get the current locale directly from AppCompatDelegate for the UI state
    val currentAppLocales = AppCompatDelegate.getApplicationLocales()
    val currentLanguageCode = if (currentAppLocales.isEmpty) "System" else currentAppLocales.toLanguageTags()

    SettingsSectionHeader(stringResource(R.string.general))
    
    // Language selection row
    SettingsClickableRow(
        title = stringResource(R.string.language),
        value = if (currentLanguageCode == "en") stringResource(R.string.english) else stringResource(R.string.system_default),
        onClick = { showLanguageDialog = true }
    )

    // Reminders row
    SettingsClickableRow(
        title = stringResource(R.string.reminders),
        value = settingsManager.getReminderFrequency(),
        onClick = onNavigateToReminders
    )

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguageCode = currentLanguageCode,
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { languageCode ->
                // 1. Persist preference in our manager
                settingsManager.setLanguage(languageCode)
                
                // 2. Apply via AppCompatDelegate
                val appLocale: LocaleListCompat = if (languageCode == "System") {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(languageCode)
                }
                
                AppCompatDelegate.setApplicationLocales(appLocale)
                showLanguageDialog = false
            }
        )
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguageCode: String,
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val options = listOf(
        "System" to stringResource(R.string.system_default),
        "en" to stringResource(R.string.english)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language)) },
        text = {
            Column {
                options.forEach { (code, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (code == currentLanguageCode),
                                onClick = { onLanguageSelected(code) }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (code == currentLanguageCode),
                            onClick = null
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
