package net.micode.moneytracker.ui.screens

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
import net.micode.moneytracker.ui.screens.settings.*
import net.micode.moneytracker.ui.theme.BeigeHeader
import net.micode.moneytracker.ui.theme.DarkBrownText
import net.micode.moneytracker.util.SettingsManager
import net.micode.moneytracker.R
/**
 * Main Settings Screen orchestrator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onCurrencyChanged: () -> Unit = {},
    selectedAccountId: Long,      // Pass selected ID
    currentAccountName: String    // Pass selected Name
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    
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
        "time_period" -> {
            TimePeriodSettingsScreen(
                settingsManager = settingsManager,
                accountName = currentAccountName,
                onBack = { currentSubScreen = null }
            )
        }
        "budget" -> {
            BudgetSettingsScreen(
                settingsManager = settingsManager,
                accountId = selectedAccountId,
                accountName = currentAccountName,
                currentCurrency = currencySymbol,
                onBack = { currentSubScreen = null }
            )
        }
        "carry_over" -> {
            CarryOverSettingsScreen(
                settingsManager = settingsManager,
                accountId = selectedAccountId,
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
                onNavigateToTimePeriod = { currentSubScreen = "time_period" },
                onNavigateToBudget = { currentSubScreen = "budget" },
                onNavigateToCarryOver = { currentSubScreen = "carry_over" },
                onNavigateToSetupPin = { isChange ->
                    isChangingPin = isChange
                    currentSubScreen = "setup_pin"
                },
                selectedAccountId = selectedAccountId,
                currentAccountName = currentAccountName
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
    onNavigateToTimePeriod: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToCarryOver: () -> Unit,
    onNavigateToSetupPin: (Boolean) -> Unit,
    selectedAccountId: Long,
    currentAccountName: String
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
                currentCurrency = currencySymbol,
                accountId = selectedAccountId,
                accountName = currentAccountName,
                onNavigateToTimePeriod = onNavigateToTimePeriod,
                onNavigateToBudget = onNavigateToBudget,
                onNavigateToCarryOver = onNavigateToCarryOver
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
