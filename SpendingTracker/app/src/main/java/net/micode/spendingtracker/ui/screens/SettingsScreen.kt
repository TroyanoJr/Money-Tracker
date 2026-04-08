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

    if (currentSubScreen == "reminders") {
        RemindersSubScreen(settingsManager = settingsManager, onBack = { currentSubScreen = null })
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
fun RemindersSubScreen(settingsManager: SettingsManager, onBack: () -> Unit) {
    val context = LocalContext.current
    var frequency by remember { mutableStateOf(settingsManager.getReminderFrequency()) }
    var reminderTime by remember { mutableStateOf(settingsManager.getReminderTime()) }
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    var showBatteryCard by remember { mutableStateOf(ReminderManager.needsBatteryExemption(context)) }
    var areNotificationsEnabled by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        areNotificationsEnabled = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notification permissions granted", Toast.LENGTH_SHORT).show()
            ReminderManager.scheduleReminder(context)
        }
    }

    LaunchedEffect(Unit) {
        areNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (areNotificationsEnabled) {
            Toast.makeText(context, "Notification permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Toast.makeText(context, "Please enable notifications in settings", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                }
                context.startActivity(intent)
            }
        }
        if (frequency != "Never" && areNotificationsEnabled) {
            ReminderManager.scheduleReminder(context)
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
                    onClick = { 
                        ReminderManager.openBatterySettings(context)
                        showBatteryCard = ReminderManager.needsBatteryExemption(context)
                    }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BatteryAlert, null, tint = Color.Red)
                        Spacer(Modifier.width(12.dp))
                        Text("Reminders may be restricted. Tap to allow background running.", color = Color.Red, fontSize = 12.sp)
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
            Toast.makeText(context, String.format(Locale.getDefault(), "Reminder time set to %02d:%02d", hour, minute), Toast.LENGTH_SHORT).show()
            showTimePicker = false
        }, { showTimePicker = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsList(settingsManager: SettingsManager, onBack: () -> Unit, onCurrencyChanged: () -> Unit, onNavigateToReminders: () -> Unit) {
    var budgetModeEnabled by remember { mutableStateOf(settingsManager.isBudgetModeEnabled()) }
    var passcodeEnabled by remember { mutableStateOf(settingsManager.isPasscodeEnabled()) }

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
            SettingsToggleRow("Budget Mode", "Enable monthly spending limit", budgetModeEnabled) { budgetModeEnabled = it; settingsManager.setBudgetModeEnabled(it) }
            SettingsClickableRow("Monthly Budget", String.format(Locale.getDefault(), "%s %.2f", settingsManager.getCurrencySymbol(), settingsManager.getMonthlyBudget()), budgetModeEnabled) { }
            SettingsToggleRow("Include Income", "Add earnings to budget", settingsManager.isIncludeIncomeInBudget(), budgetModeEnabled) { settingsManager.setIncludeIncomeInBudget(it) }

            SettingsSectionHeader("Security")
            SettingsToggleRow("Passcode Lock", "Require a PIN to open", passcodeEnabled) { passcodeEnabled = it; settingsManager.setPasscodeEnabled(it) }

            SettingsSectionHeader("User Interface")
            SettingsClickableRow("Currency Symbol", settingsManager.getCurrencySymbol()) { }

            SettingsSectionHeader("General")
            SettingsClickableRow("Reminders", settingsManager.getReminderFrequency(), onClick = onNavigateToReminders)
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
    var isInputMode by remember { mutableStateOf(false) }
    val state = rememberTimePickerState(hour, minute, true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton({ onConfirm(state.hour, state.minute) }) { Text("OK", color = DarkBrownText, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onDismiss) { Text("CANCEL", color = DarkBrownText) } },
        title = { Text("Set Time", color = DarkBrownText) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isInputMode) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Enter time", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                        TimeInput(state)
                    }
                } else {
                    TimePicker(state)
                }
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    IconButton({ isInputMode = !isInputMode }) {
                        Icon(if (isInputMode) Icons.Default.AccessTime else Icons.Default.Keyboard, null, tint = DarkBrownText)
                    }
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    )
}
