package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import net.micode.spendingtracker.R
import net.micode.spendingtracker.ui.screens.settings.*
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.util.SettingsManager

/**
 * Main Settings Screen orchestrator.
 * Centralizes state management for currency and navigation between modular sections.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onCurrencyChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    
    // Shared reactive state for currency to ensure UI sync across sections
    var currencySymbol by remember { mutableStateOf(settingsManager.getCurrencySymbol()) }
    
    var currentSubScreen by rememberSaveable { mutableStateOf<String?>(null) }
    var isChangingPin by rememberSaveable { mutableStateOf(false) }

    when (currentSubScreen) {
        "reminders" -> {
            RemindersScreen(
                settingsManager = settingsManager,
                onBack = { currentSubScreen = null }
            )
        }
        "setup_pin" -> {
            PinEntryScreen(
                settingsManager = settingsManager,
                isChangeMode = isChangingPin,
                onBack = { 
                    currentSubScreen = null
                    isChangingPin = false
                },
                onSuccess = {
                    settingsManager.setPasscodeEnabled(true)
                    currentSubScreen = null
                    isChangingPin = false
                }
            )
        }
        else -> {
            MainSettingsList(
                settingsManager = settingsManager,
                currencySymbol = currencySymbol,
                onBack = onBack,
                onCurrencyChanged = {
                    currencySymbol = settingsManager.getCurrencySymbol()
                    onCurrencyChanged()
                },
                onNavigateToReminders = { currentSubScreen = "reminders" },
                onNavigateToSetupPin = { isChange ->
                    isChangingPin = isChange
                    currentSubScreen = "setup_pin"
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsList(
    settingsManager: SettingsManager,
    currencySymbol: String,
    onBack: () -> Unit,
    onCurrencyChanged: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToSetupPin: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), color = DarkBrownText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = DarkBrownText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BeigeHeader)
            )
        },
        containerColor = BeigeHeader
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SpendingSection(
                settingsManager = settingsManager,
                currentCurrency = currencySymbol
            )
            
            SecuritySection(
                settingsManager = settingsManager,
                onNavigateToSetupPin = onNavigateToSetupPin
            )
            
            InterfaceSection(
                settingsManager = settingsManager,
                onCurrencyChanged = onCurrencyChanged
            )
            
            GeneralSection(
                settingsManager = settingsManager,
                onNavigateToReminders = onNavigateToReminders
            )
        }
    }
}
