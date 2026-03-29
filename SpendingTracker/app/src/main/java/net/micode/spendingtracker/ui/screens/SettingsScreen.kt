package net.micode.spendingtracker.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.NotificationManagerCompat
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.BlackboardBlack
import net.micode.spendingtracker.ui.theme.ChalkWhite
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.util.ReminderManager
import net.micode.spendingtracker.util.SettingsManager
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onCurrencyChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    var currentSubScreen by rememberSaveable { mutableStateOf<String?>(null) }

    if (currentSubScreen == "reminders") {
        RemindersSubScreen(
            settingsManager = settingsManager,
            onBack = { currentSubScreen = null }
        )
    } else {
        MainSettingsList(
            settingsManager = settingsManager,
            onBack = onBack,
            onCurrencyChanged = onCurrencyChanged,
            onNavigateToReminders = {
                Toast.makeText(context, "Confirming reminder settings...", Toast.LENGTH_SHORT).show()
                currentSubScreen = "reminders"
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersSubScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var frequency by remember { mutableStateOf(settingsManager.getReminderFrequency()) }
    var reminderTime by remember { mutableStateOf(settingsManager.getReminderTime()) }
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val isOptimized = remember { ReminderManager.isBatteryOptimized(context) }
    var areNotificationsEnabled by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        areNotificationsEnabled = isGranted
        if (isGranted) Toast.makeText(context, "Notification permissions granted", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        if (areNotificationsEnabled) {
            Toast.makeText(context, "Notification permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // REDIRECCIÓN MANUAL PARA HUAWEI/ANDROID 12
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            }
        }
        if (frequency != "Never") ReminderManager.scheduleReminder(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders", color = DarkBrownText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkBrownText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BeigeHeader)
            )
        },
        containerColor = BeigeHeader
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            
            // BOTÓN DE RESCATE: Si en Huawei las notificaciones fallan aunque diga que están activas
            if (!areNotificationsEnabled) {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("ENABLE NOTIFICATIONS")
                }
            }

            if (isOptimized && frequency != "Never") {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    onClick = { ReminderManager.openBatterySettings(context) }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BatteryAlert, contentDescription = null, tint = Color.Red)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "CRITICAL: To receive reminders after closing the app, tap here and set to 'Manage Manually'.",
                            color = Color.Red, fontSize = 12.sp
                        )
                    }
                }
            }

            SettingsSectionHeader("Schedule")
            SettingsClickableRow(title = "Frequency", value = frequency, onClick = { showFrequencyDialog = true })
            SettingsClickableRow(title = "Time", value = String.format(Locale.getDefault(), "%02d:%02d", reminderTime.first, reminderTime.second), onClick = { showTimePicker = true })
        }
    }

    if (showFrequencyDialog) {
        FrequencySelectionDialog(
            currentFrequency = frequency,
            onDismiss = { showFrequencyDialog = false },
            onSelect = { selectedFreq ->
                frequency = selectedFreq
                settingsManager.setReminderFrequency(selectedFreq)
                ReminderManager.scheduleReminder(context)
                Toast.makeText(context, "Frequency updated: $selectedFreq", Toast.LENGTH_SHORT).show()
                showFrequencyDialog = false
            }
        )
    }

    if (showTimePicker) {
        AdvancedTimePickerDialog(
            initialHour = reminderTime.first,
            initialMinute = reminderTime.second,
            onTimeSelected = { hour, minute ->
                reminderTime = Pair(hour, minute)
                settingsManager.setReminderTime(hour, minute)
                ReminderManager.scheduleReminder(context)
                Toast.makeText(context, String.format(Locale.getDefault(), "Reminder time set to %02d:%02d", hour, minute), Toast.LENGTH_SHORT).show()
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

// Visual components
@Composable
fun SettingsSectionHeader(title: String) {
    Box(modifier = Modifier.fillMaxWidth().background(Color.LightGray.copy(alpha = 0.3f)).padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(title, color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SettingsToggleRow(title: String, subtitle: String, checked: Boolean, enabled: Boolean = true, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = if (enabled) Color(0xFF1976D2) else Color.Gray)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
}

@Composable
fun SettingsClickableRow(title: String, value: String, enabled: Boolean = true, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { onClick() }.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 16.sp, color = if (enabled) Color(0xFF1976D2) else Color.Gray)
        Text(value, fontSize = 16.sp, color = if (enabled) Color(0xFF1976D2) else Color.Gray)
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
}

@Composable
fun FrequencySelectionDialog(currentFrequency: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val options = listOf("Every Week", "Every Day", "Every Month", "Never")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Frequency", color = DarkBrownText) },
        text = {
            Column {
                options.forEach { option ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { onSelect(option) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = currentFrequency == option, onClick = { onSelect(option) })
                        Spacer(Modifier.width(16.dp))
                        Text(option, color = Color.Black, fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) } },
        containerColor = Color.White,
        shape = RoundedCornerShape(4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedTimePickerDialog(initialHour: Int, initialMinute: Int, onTimeSelected: (Int, Int) -> Unit, onDismiss: () -> Unit) {
    var isInputMode by remember { mutableStateOf(false) }
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onTimeSelected(state.hour, state.minute) }) { Text("OK", color = DarkBrownText, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = { onDismiss() }) { Text("CANCEL", color = DarkBrownText) } },
        title = { Text("Set Time", color = DarkBrownText) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if (isInputMode) { TimeInput(state = state) } else { TimePicker(state = state) }
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    IconButton(onClick = { isInputMode = !isInputMode }) {
                        Icon(imageVector = if (isInputMode) Icons.Default.AccessTime else Icons.Default.Keyboard, contentDescription = null, tint = DarkBrownText)
                    }
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsList(
    settingsManager: SettingsManager,
    onBack: () -> Unit,
    onCurrencyChanged: () -> Unit,
    onNavigateToReminders: () -> Unit
) {
    var currentCurrency by remember { mutableStateOf(settingsManager.getCurrencySymbol()) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var budgetModeEnabled by remember { mutableStateOf(settingsManager.isBudgetModeEnabled()) }
    var monthlyBudgetAmount by remember { mutableStateOf(settingsManager.getMonthlyBudget()) }
    var includeIncomeEnabled by remember { mutableStateOf(settingsManager.isIncludeIncomeInBudget()) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var passcodeEnabled by remember { mutableStateOf(settingsManager.isPasscodeEnabled()) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var pinSetupMode by remember { mutableStateOf("CREATE") }
    var reminderFrequency by remember { mutableStateOf(settingsManager.getReminderFrequency()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = DarkBrownText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkBrownText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BeigeHeader)
            )
        },
        containerColor = BeigeHeader
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader("Spending")
            SettingsToggleRow(title = "Budget Mode", subtitle = "Enable monthly spending limit", checked = budgetModeEnabled, onCheckedChange = { budgetModeEnabled = it; settingsManager.setBudgetModeEnabled(it) })
            SettingsClickableRow(title = "Monthly Budget", value = String.format(Locale.getDefault(), "%s %.2f", currentCurrency, monthlyBudgetAmount), enabled = budgetModeEnabled, onClick = { showBudgetDialog = true })
            SettingsToggleRow(title = "Include Income", subtitle = "Add monthly earnings to your budget limit", checked = includeIncomeEnabled, enabled = budgetModeEnabled, onCheckedChange = { includeIncomeEnabled = it; settingsManager.setIncludeIncomeInBudget(it) })

            SettingsSectionHeader("Security")
            SettingsToggleRow(title = "Passcode Lock", subtitle = "Require a PIN to open the app", checked = passcodeEnabled, onCheckedChange = { enabled ->
                if (enabled) { pinSetupMode = "CREATE"; showPinSetupDialog = true } else {
                    if (settingsManager.getPasscode().isNotEmpty()) { pinSetupMode = "DISABLE"; showPinSetupDialog = true }
                    else { passcodeEnabled = false; settingsManager.setPasscodeEnabled(false) }
                }
            })
            SettingsClickableRow(title = "Lock Type", value = "PIN", enabled = passcodeEnabled, onClick = { if (passcodeEnabled) { pinSetupMode = "CHANGE"; showPinSetupDialog = true } })

            SettingsSectionHeader("User Interface")
            SettingsClickableRow(title = "Currency Symbol", value = if (currentCurrency == "¥") "Default (¥)" else currentCurrency, onClick = { showCurrencyDialog = true })

            SettingsSectionHeader("General")
            SettingsClickableRow(title = "Reminders", value = reminderFrequency, onClick = onNavigateToReminders)
        }
    }

    if (showBudgetDialog) { BudgetSetupDialog(currentAmount = monthlyBudgetAmount, currencySymbol = currentCurrency, onDismiss = { showBudgetDialog = false }, onConfirm = { amount -> monthlyBudgetAmount = amount; settingsManager.setMonthlyBudget(amount); showBudgetDialog = false }) }
    if (showCurrencyDialog) { CurrencySelectionDialog(currentSymbol = currentCurrency, onDismiss = { showCurrencyDialog = false }, onSelect = { symbol -> currentCurrency = symbol; settingsManager.setCurrencySymbol(symbol); showCurrencyDialog = false; onCurrencyChanged() }) }
    if (showPinSetupDialog) { PinSetupDialog(mode = pinSetupMode, currentSavedPin = settingsManager.getPasscode(), onDismiss = { showPinSetupDialog = false }, onConfirm = { newPin ->
        if (pinSetupMode == "DISABLE") { settingsManager.setPasscodeEnabled(false); passcodeEnabled = false }
        else { settingsManager.setPasscode(newPin); settingsManager.setPasscodeEnabled(true); passcodeEnabled = true }
        showPinSetupDialog = false
    }) }
}

@Composable
fun BudgetSetupDialog(currentAmount: Double, currencySymbol: String, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var text by remember { mutableStateOf(currentAmount.toString()) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Monthly Budget") }, text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Amount ($currencySymbol)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)) }, confirmButton = { TextButton(onClick = { onConfirm(text.toDoubleOrNull() ?: 0.0) }) { Text("SAVE") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } })
}

@Composable
fun CurrencySelectionDialog(currentSymbol: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val symbols = listOf("¥", "$", "€", "£")
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Select Currency") }, text = { Column(modifier = Modifier.verticalScroll(rememberScrollState())) { symbols.forEach { s -> Row(modifier = Modifier.fillMaxWidth().clickable { onSelect(s) }.padding(12.dp)) { RadioButton(selected = currentSymbol == s, onClick = { onSelect(s) }); Text(s, modifier = Modifier.padding(start = 8.dp)) } } } }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } })
}

@Composable
fun PinSetupDialog(mode: String, currentSavedPin: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    Dialog(onDismissRequest = onDismiss) { Surface(modifier = Modifier.fillMaxSize(), color = BlackboardBlack) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text("PIN Setup", color = ChalkWhite, fontSize = 24.sp, fontFamily = FontFamily.Cursive); Spacer(Modifier.height(32.dp)); Button(onClick = { onConfirm("1234") }) { Text("Set 1234 as PIN (Test Only)") }; TextButton(onClick = onDismiss) { Text("CANCEL", color = ChalkWhite) } } } }
}
