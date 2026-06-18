package net.micode.moneytracker.ui.screens.settings

import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import net.micode.moneytracker.util.SettingsManager

/**
 * Section for Security settings (PIN, etc).
 * Fully localized using string resources.
 */
@Composable
fun SecuritySection(
    settingsManager: SettingsManager,
    onNavigateToSetupPin: (Boolean) -> Unit
) {
    var passcodeEnabled by remember { mutableStateOf(settingsManager.isPasscodeEnabled()) }

    SettingsSectionHeader(stringResource(R.string.security))
    
    SettingsToggleRow(
        title = stringResource(R.string.passcode_lock),
        subtitle = stringResource(R.string.passcode_lock_desc),
        checked = passcodeEnabled
    ) { enabled ->
        if (enabled && settingsManager.getPasscode().isEmpty()) {
            // Initial setup flow
            onNavigateToSetupPin(false)
        } else {
            passcodeEnabled = enabled
            settingsManager.setPasscodeEnabled(enabled)
        }
    }

    if (passcodeEnabled) {
        SettingsClickableRow(
            title = stringResource(R.string.change_pin),
            value = stringResource(R.string.change_pin_desc)
        ) { 
            // Trigger change flow (requires current PIN verification)
            onNavigateToSetupPin(true)
        }
    }
}
