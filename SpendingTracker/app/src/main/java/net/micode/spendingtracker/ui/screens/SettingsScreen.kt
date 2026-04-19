package net.micode.spendingtracker.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.util.ReminderManager
import net.micode.spendingtracker.util.SettingsManager
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, onCurrencyChanged: () -> Unit = {}) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    var currentSubScreen by rememberSaveable { mutableStateOf<String?>(null) }

    when (currentSubScreen) {
        "reminders" -> {
            RemindersSubScreen(settingsManager = settingsManager, onBack = { currentSubScreen = null })
        }
        "setup_pin" -> {
            PinEntryScreen(
                settingsManager = settingsManager,
                onBack = { currentSubScreen = null },
                onSuccess = {
                    settingsManager.setPasscodeEnabled(true)
                    currentSubScreen = null
                    Toast.makeText(context, "Security PIN updated", Toast.LENGTH_SHORT).show()
                }
            )
        }
        else -> {
            MainSettingsList(
                settingsManager = settingsManager,
                onBack = onBack,
                onCurrencyChanged = onCurrencyChanged,
                onNavigateToReminders = { currentSubScreen = "reminders" },
                onNavigateToSetupPin = { currentSubScreen = "setup_pin" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersSubScreen(settingsManager: SettingsManager, onBack: () -> Unit) {
    val context = LocalContext.current
    var frequency by remember { mutableStateOf(settingsManager.getReminderFrequency()) }
    var reminderTime by remember { mutableStateOf(settingsManager.getReminderTime()) }
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showBatteryInstructionDialog by remember { mutableStateOf(false) }
    var showBatteryCard by remember { mutableStateOf(ReminderManager.needsBatteryExemption(context)) }
    var areNotificationsEnabled by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        areNotificationsEnabled = isGranted
        if (isGranted) ReminderManager.scheduleReminder(context)
    }

    LaunchedEffect(Unit) {
        areNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!areNotificationsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders", color = DarkBrownText, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkBrownText) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BeigeHeader)
            )
        },
        containerColor = BeigeHeader
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            if (showBatteryCard && frequency != "Never") {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    onClick = { showBatteryInstructionDialog = true }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BatteryAlert, null, tint = Color.Red)
                        Spacer(Modifier.width(12.dp))
                        Text("Reminders may be restricted. Tap for instructions.", color = Color.Red, fontSize = 12.sp)
                    }
                }
            }
            SettingsSectionHeader("Schedule")
            SettingsClickableRow("Frequency", frequency) { showFrequencyDialog = true }
            SettingsClickableRow("Time", String.format(Locale.getDefault(), "%02d:%02d", reminderTime.first, reminderTime.second)) { showTimePicker = true }
        }
    }

    if (showFrequencyDialog) {
        FrequencySelectionDialog(frequency, { showFrequencyDialog = false }, { selected ->
            frequency = selected
            settingsManager.setReminderFrequency(selected)
            ReminderManager.scheduleReminder(context)
            showFrequencyDialog = false
        })
    }

    if (showTimePicker) {
        AdvancedTimePickerDialog(reminderTime.first, reminderTime.second, { hour, minute ->
            reminderTime = Pair(hour, minute)
            settingsManager.setReminderTime(hour, minute)
            ReminderManager.scheduleReminder(context)
            showTimePicker = false
        }, { showTimePicker = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsList(
    settingsManager: SettingsManager, 
    onBack: () -> Unit, 
    onCurrencyChanged: () -> Unit, 
    onNavigateToReminders: () -> Unit,
    onNavigateToSetupPin: () -> Unit
) {
    var budgetModeEnabled by remember { mutableStateOf(settingsManager.isBudgetModeEnabled()) }
    var includeIncome by remember { mutableStateOf(settingsManager.isIncludeIncomeInBudget()) }
    var monthlyBudget by remember { mutableStateOf(settingsManager.getMonthlyBudget()) }
    var passcodeEnabled by remember { mutableStateOf(settingsManager.isPasscodeEnabled()) }
    var currentCurrency by remember { mutableStateOf(settingsManager.getCurrencySymbol()) }
    
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = DarkBrownText, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkBrownText) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BeigeHeader)
            )
        },
        containerColor = BeigeHeader
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            SettingsSectionHeader("Spending")
            SettingsToggleRow("Budget Mode", "Enable monthly spending limit", budgetModeEnabled) { 
                budgetModeEnabled = it
                settingsManager.setBudgetModeEnabled(it) 
            }
            SettingsClickableRow(
                "Monthly Budget", 
                String.format(Locale.getDefault(), "%s %.2f", currentCurrency, monthlyBudget), 
                budgetModeEnabled
            ) { showBudgetDialog = true }
            SettingsToggleRow("Include Income", "Add earnings to budget", includeIncome, budgetModeEnabled) { 
                includeIncome = it
                settingsManager.setIncludeIncomeInBudget(it) 
            }

            SettingsSectionHeader("Security")
            SettingsToggleRow("Passcode Lock", "Require a PIN to open", passcodeEnabled) { 
                if (it && settingsManager.getPasscode().isEmpty()) {
                    onNavigateToSetupPin()
                } else {
                    passcodeEnabled = it
                    settingsManager.setPasscodeEnabled(it)
                }
            }
            if (passcodeEnabled) {
                SettingsClickableRow("Change PIN", "Update your secret code") { onNavigateToSetupPin() }
            }

            SettingsSectionHeader("User Interface")
            SettingsClickableRow("Currency Symbol", currentCurrency) { 
                showCurrencyDialog = true 
            }

            SettingsSectionHeader("General")
            SettingsClickableRow("Reminders", settingsManager.getReminderFrequency(), onClick = onNavigateToReminders)
        }
    }

    if (showBudgetDialog) {
        BudgetAmountDialog(
            currentAmount = monthlyBudget,
            currencySymbol = currentCurrency,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { 
                monthlyBudget = it
                settingsManager.setMonthlyBudget(it)
                showBudgetDialog = false
            }
        )
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currentSymbol = currentCurrency,
            onDismiss = { showCurrencyDialog = false },
            onSelect = { 
                currentCurrency = it
                settingsManager.setCurrencySymbol(it)
                onCurrencyChanged()
                showCurrencyDialog = false
            }
        )
    }
}

@Composable
fun CurrencySelectionDialog(currentSymbol: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val commonSymbols = listOf("$", "€", "£", "¥", "₹", "₩", "₽", "฿", "₫", "₪", "₱", "₡", "₲", "₵", "₸", "₺", "₦", "₴", "SR", "RM", "RP", "Kr", "R$", "L")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency Symbol") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(300.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(commonSymbols) { symbol ->
                    val isSelected = symbol == currentSymbol
                    Box(modifier = Modifier.aspectRatio(1f).background(if (isSelected) Color(0xFF1976D2).copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(8.dp)).border(1.dp, if (isSelected) Color(0xFF1976D2) else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).clickable { onSelect(symbol) }, contentAlignment = Alignment.Center) {
                        Text(symbol, fontSize = 18.sp, color = if (isSelected) Color(0xFF1976D2) else Color.Black)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("CLOSE", color = Color.Gray) } },
        containerColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun PinEntryScreen(settingsManager: SettingsManager, onBack: () -> Unit, onSuccess: () -> Unit) {
    var step by remember { mutableStateOf(if (settingsManager.getPasscode().isEmpty()) 1 else 3) } 
    var firstPin by remember { mutableStateOf("") }
    var currentInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val title = when(step) { 1 -> "Create Secret PIN"; 2 -> "Confirm PIN"; else -> "Enter Secret PIN" }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1A1A1A)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(title, color = Color.White, fontSize = 32.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Light)
            Text("Keep your chalk drawings safe", color = Color.Gray, fontSize = 14.sp, fontStyle = FontStyle.Italic, modifier = Modifier.padding(top = 8.dp, bottom = 48.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 64.dp)) {
                repeat(4) { index ->
                    val isFilled = index < currentInput.length
                    Box(modifier = Modifier.size(24.dp).border(1.dp, Color.Gray, CircleShape).padding(4.dp).background(if (isFilled) Color.White.copy(alpha = 0.8f) else Color.Transparent, CircleShape))
                }
            }
            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "DEL")
            Column(modifier = Modifier.width(280.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                for (row in 0 until 4) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        for (col in 0 until 3) {
                            val key = keys[row * 3 + col]
                            if (key.isNotEmpty()) {
                                Box(modifier = Modifier.size(80.dp).clickable {
                                    if (key == "DEL") { if (currentInput.isNotEmpty()) currentInput = currentInput.dropLast(1) }
                                    else if (currentInput.length < 4) {
                                        currentInput += key
                                        if (currentInput.length == 4) {
                                            when(step) {
                                                1 -> { firstPin = currentInput; currentInput = ""; step = 2 }
                                                2 -> { if (currentInput == firstPin) { settingsManager.setPasscode(currentInput); onSuccess() } else { Toast.makeText(context, "PINs don't match", Toast.LENGTH_SHORT).show(); currentInput = ""; step = 1 } }
                                                3 -> { if (currentInput == settingsManager.getPasscode()) onSuccess() else { Toast.makeText(context, "Wrong PIN", Toast.LENGTH_SHORT).show(); currentInput = "" } }
                                            }
                                        }
                                    }
                                }, contentAlignment = Alignment.Center) {
                                    if (key == "DEL") Icon(Icons.Default.Backspace, null, tint = Color.White, modifier = Modifier.size(32.dp))
                                    else Text(key, color = Color.White, fontSize = 36.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Light)
                                }
                            } else Spacer(Modifier.size(80.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp)); TextButton(onClick = onBack) { Text("CANCEL", color = Color.White.copy(alpha = 0.5f)) }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Box(modifier = Modifier.fillMaxWidth().background(Color.LightGray.copy(alpha = 0.3f)).padding(16.dp, 8.dp)) {
        Text(title, color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SettingsToggleRow(title: String, subtitle: String, checked: Boolean, enabled: Boolean = true, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = if (enabled) Color(0xFF1976D2) else Color.Gray)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(checked, onCheckedChange, enabled = enabled)
    }
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), 0.5.dp, Color.LightGray)
}

@Composable
fun SettingsClickableRow(title: String, value: String, enabled: Boolean = true, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(enabled) { onClick() }.padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(title, fontSize = 16.sp, color = if (enabled) Color(0xFF1976D2) else Color.Gray)
        Text(value, fontSize = 16.sp, color = if (enabled) Color(0xFF1976D2) else Color.Gray)
    }
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), 0.5.dp, Color.LightGray)
}

@Composable
fun FrequencySelectionDialog(current: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val options = listOf("Every Week", "Every Day", "Every Month", "Never")
    AlertDialog(onDismiss, title = { Text("Frequency") }, text = { Column { options.forEach { Row(Modifier.fillMaxWidth().clickable { onSelect(it) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(current == it, null); Spacer(Modifier.width(8.dp)); Text(it) } } } }, confirmButton = {}, dismissButton = { TextButton(onDismiss) { Text("CANCEL") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedTimePickerDialog(hour: Int, minute: Int, onConfirm: (Int, Int) -> Unit, onDismiss: () -> Unit) {
    var isInputMode by remember { mutableStateOf(false) } // Added state for input mode toggle
    val state = rememberTimePickerState(hour, minute, true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton({ onConfirm(state.hour, state.minute) }) { Text("OK", color = DarkBrownText, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onDismiss) { Text("CANCEL", color = DarkBrownText) } },
        title = { Text("Set Time", color = DarkBrownText) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Toggle between keyboard input and clock face
                if (isInputMode) {
                    TimeInput(state)
                } else {
                    TimePicker(state)
                }
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    IconButton(onClick = { isInputMode = !isInputMode }) {
                        Icon(if (isInputMode) Icons.Default.AccessTime else Icons.Default.Keyboard, contentDescription = null, tint = DarkBrownText)
                    }
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun BudgetAmountDialog(currentAmount: Double, currencySymbol: String, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var textValue by remember { mutableStateOf(currentAmount.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monthly Budget") },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text("Amount ($currencySymbol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(textValue.toDoubleOrNull() ?: 0.0) }) { Text("SAVE", color = DarkBrownText, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) } },
        containerColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    )
}
