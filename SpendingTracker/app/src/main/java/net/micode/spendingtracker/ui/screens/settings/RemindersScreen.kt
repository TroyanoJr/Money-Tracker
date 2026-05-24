package net.micode.spendingtracker.ui.screens.settings

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import net.micode.spendingtracker.R
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.util.ReminderManager
import net.micode.spendingtracker.util.SettingsManager
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    var frequency by remember { mutableStateOf(settingsManager.getReminderFrequency()) }
    var reminderTime by remember { mutableStateOf(settingsManager.getReminderTime()) }
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showBatteryInstructionDialog by remember { mutableStateOf(false) }
    
    val showBatteryCard = ReminderManager.needsBatteryExemption(context)

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) ReminderManager.scheduleReminder(context)
    }

    // Proactive permission handling (similar to Pixel 7)
    LaunchedEffect(Unit) {
        // Check Notification Permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Check Exact Alarm Permission (Android 12+) - Critical for punctual reminders
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to general settings if specific intent fails
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reminders), color = DarkBrownText, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = DarkBrownText) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BeigeHeader)
            )
        },
        containerColor = BeigeHeader
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            
            // Manual configuration card for Huawei/Aggressive battery managers
            if (showBatteryCard && frequency != "Never") {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    onClick = { showBatteryInstructionDialog = true }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BatteryAlert, null, tint = Color.Red)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.reminder_restriction_warn), color = Color.Red, fontSize = 12.sp)
                    }
                }
            }

            SettingsSectionHeader(stringResource(R.string.general))
            SettingsClickableRow(stringResource(R.string.frequency), frequency) { showFrequencyDialog = true }
            SettingsClickableRow(
                title = stringResource(R.string.time), 
                value = String.format(Locale.getDefault(), "%02d:%02d", reminderTime.first, reminderTime.second)
            ) { showTimePicker = true }
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

    if (showBatteryInstructionDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryInstructionDialog = false },
            title = { Text(stringResource(R.string.manual_config_required), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.huawei_battery_instruction)) },
            confirmButton = {
                TextButton(onClick = { 
                    showBatteryInstructionDialog = false
                    val intent = Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                    context.startActivity(intent)
                }) { Text(stringResource(R.string.go_to_settings), color = DarkBrownText) }
            },
            dismissButton = {
                TextButton(onClick = { showBatteryInstructionDialog = false }) {
                    Text(stringResource(R.string.close), color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun FrequencySelectionDialog(current: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val options = listOf(
        stringResource(R.string.every_week),
        stringResource(R.string.every_day),
        stringResource(R.string.every_month),
        stringResource(R.string.never)
    )
    AlertDialog(
        onDismiss, 
        title = { Text(stringResource(R.string.frequency)) }, 
        text = { 
            Column { 
                options.forEach { option -> 
                    Row(Modifier.fillMaxWidth().clickable { onSelect(option) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { 
                        RadioButton(current == option, null)
                        Spacer(Modifier.width(8.dp))
                        Text(option) 
                    } 
                } 
            } 
        }, 
        confirmButton = {}, 
        dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedTimePickerDialog(hour: Int, minute: Int, onConfirm: (Int, Int) -> Unit, onDismiss: () -> Unit) {
    var isInputMode by remember { mutableStateOf(false) }
    val state = rememberTimePickerState(hour, minute, true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton({ onConfirm(state.hour, state.minute) }) { Text(stringResource(R.string.ok), color = DarkBrownText, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.cancel), color = DarkBrownText) } },
        title = { Text(stringResource(R.string.set_time), color = DarkBrownText) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isInputMode) TimeInput(state) else TimePicker(state)
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    IconButton(onClick = { isInputMode = !isInputMode }) {
                        Icon(if (isInputMode) Icons.Default.AccessTime else Icons.Default.Keyboard, null, tint = DarkBrownText)
                    }
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    )
}
