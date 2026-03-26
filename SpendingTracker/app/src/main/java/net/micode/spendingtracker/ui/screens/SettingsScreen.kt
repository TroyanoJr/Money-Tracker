package net.micode.spendingtracker.ui.screens

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
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Sell
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
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
            onNavigateToReminders = { currentSubScreen = "reminders" }
        )
    }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader("Spending")
            SettingsToggleRow(
                title = "Budget Mode",
                subtitle = "Enable monthly spending limit",
                checked = budgetModeEnabled,
                onCheckedChange = {
                    budgetModeEnabled = it
                    settingsManager.setBudgetModeEnabled(it)
                }
            )
            SettingsClickableRow(
                title = "Monthly Budget",
                value = String.format(Locale.getDefault(), "%s %.2f", currentCurrency, monthlyBudgetAmount),
                enabled = budgetModeEnabled,
                onClick = { showBudgetDialog = true }
            )
            SettingsToggleRow(
                title = "Include Income",
                subtitle = "Add monthly earnings to your budget limit",
                checked = includeIncomeEnabled,
                enabled = budgetModeEnabled,
                onCheckedChange = {
                    includeIncomeEnabled = it
                    settingsManager.setIncludeIncomeInBudget(it)
                }
            )

            SettingsSectionHeader("Security")
            SettingsToggleRow(
                title = "Passcode Lock",
                subtitle = "Require a PIN to open the app",
                checked = passcodeEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        pinSetupMode = "CREATE"
                        showPinSetupDialog = true
                    } else {
                        if (settingsManager.getPasscode().isNotEmpty()) {
                            pinSetupMode = "DISABLE"
                            showPinSetupDialog = true
                        } else {
                            passcodeEnabled = false
                            settingsManager.setPasscodeEnabled(false)
                        }
                    }
                }
            )
            SettingsClickableRow(
                title = "Lock Type",
                value = "PIN",
                enabled = passcodeEnabled,
                onClick = { 
                    if (passcodeEnabled) {
                        pinSetupMode = "CHANGE"
                        showPinSetupDialog = true
                    }
                }
            )

            SettingsSectionHeader("User Interface")
            SettingsClickableRow(
                title = "Currency Symbol",
                value = if (currentCurrency == "¥") "Default (¥)" else currentCurrency,
                onClick = { showCurrencyDialog = true }
            )

            SettingsSectionHeader("General")
            SettingsClickableRow(
                title = "Reminders",
                value = reminderFrequency,
                onClick = onNavigateToReminders
            )
        }
    }

    if (showBudgetDialog) {
        BudgetSetupDialog(
            currentAmount = monthlyBudgetAmount,
            currencySymbol = currentCurrency,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { amount ->
                monthlyBudgetAmount = amount
                settingsManager.setMonthlyBudget(amount)
                showBudgetDialog = false
            }
        )
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currentSymbol = currentCurrency,
            onDismiss = { showCurrencyDialog = false },
            onSelect = { symbol ->
                currentCurrency = symbol
                settingsManager.setCurrencySymbol(symbol)
                showCurrencyDialog = false
                onCurrencyChanged()
            }
        )
    }

    if (showPinSetupDialog) {
        PinSetupDialog(
            mode = pinSetupMode,
            currentSavedPin = settingsManager.getPasscode(),
            onDismiss = { showPinSetupDialog = false },
            onConfirm = { newPin ->
                if (pinSetupMode == "DISABLE") {
                    settingsManager.setPasscodeEnabled(false)
                    passcodeEnabled = false
                } else {
                    settingsManager.setPasscode(newPin)
                    settingsManager.setPasscodeEnabled(true)
                    passcodeEnabled = true
                }
                showPinSetupDialog = false
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SettingsSectionHeader("Reminders")
            
            SettingsClickableRow(
                title = "Frequency",
                value = frequency,
                onClick = { showFrequencyDialog = true }
            )
            
            SettingsClickableRow(
                title = "Time",
                value = String.format(Locale.getDefault(), "%02d:%02d", reminderTime.first, reminderTime.second),
                onClick = { showTimePicker = true }
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "You can choose how frequently you want to be reminded to enter your transactions. You will receive a notification on your device to remind you.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 20.sp
            )
        }
    }

    if (showFrequencyDialog) {
        FrequencySelectionDialog(
            currentFrequency = frequency,
            onDismiss = { showFrequencyDialog = false },
            onSelect = {
                frequency = it
                settingsManager.setReminderFrequency(it)
                ReminderManager.scheduleReminder(context)
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
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun FrequencySelectionDialog(
    currentFrequency: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val options = listOf("Every Week", "Every Day", "Every Month", "Never")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Frequency", color = DarkBrownText) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentFrequency == option,
                            onClick = { onSelect(option) },
                            colors = RadioButtonDefaults.colors(selectedColor = DarkBrownText)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(option, color = Color.Black, fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = DarkBrownText)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var isInputMode by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {
            TextButton(onClick = { onTimeSelected(timePickerState.hour, timePickerState.minute) }) {
                Text("确定", color = DarkBrownText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = DarkBrownText)
            }
        },
        title = {
            Text(text = "设置时间", fontSize = 20.sp, color = DarkBrownText)
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                if (isInputMode) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("请输入时间", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                        TimeInput(state = timePickerState)
                    }
                } else {
                    TimePicker(state = timePickerState)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    IconButton(onClick = { isInputMode = !isInputMode }) {
                        Icon(
                            imageVector = if (isInputMode) Icons.Default.AccessTime else Icons.Default.Keyboard,
                            contentDescription = "Toggle Input Mode",
                            tint = DarkBrownText
                        )
                    }
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun SettingsSectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SettingsToggleRow(
    title: String, 
    subtitle: String, 
    checked: Boolean, 
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = if (enabled) Color(0xFF1976D2) else Color.Gray)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DarkBrownText,
                checkedTrackColor = DarkBrownText.copy(alpha = 0.4f)
            )
        )
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
}

@Composable
fun SettingsClickableRow(title: String, value: String, enabled: Boolean = true, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 16.sp, color = if (enabled) Color(0xFF1976D2) else Color.Gray)
        Text(value, fontSize = 16.sp, color = if (enabled) Color(0xFF1976D2) else Color.Gray)
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
}

@Composable
fun CurrencySelectionDialog(currentSymbol: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val options = listOf(
        "¥ (JPY/CNY)", "$ (USD)", "€ (EUR)", "£ (GBP)",
        "A$ (AUD)", "C$ (CAD)", "CHF (CHF)", "HK$ (HKD)",
        "S$ (SGD)", "NZ$ (NZD)", "₹ (INR)", "₩ (KRW)",
        "₽ (RUB)", "₺ (TRY)", "฿ (THB)", "R$ (BRL)",
        "S/. (PEN)", "MXN (MXN)", "COP$ (COP)", "AR$ (ARS)",
        "CLP$ (CLP)", "RD$ (DOP)", "₡ (CRC)", "Q (GTQ)",
        "L (HNL)", "C$ (NIO)", "B/. (PAB)", "₲ (PYG)",
        "UYU (UYU)", "Bs (VES)", "R (ZAR)", "₱ (PHP)",
        "RM (MYR)", "Rp (IDR)", "₫ (VND)", "zł (PLN)",
        "kr (SEK/NOK/DKK)", "Ft (HUF)", "Kč (CZK)", "₪ (ILS)",
        "₨ (PKR)", "E£ (EGP)", "SR (SAR)", "AED (AED)",
        "QR (QAR)", "KD (KWD)", "BD (BHD)", "OMR (OMR)",
        "JOD (JOD)", "₴ (UAH)", "L (RON)", "лв (BGN)",
        "din (RSD)", "DH (MAD)", "DA (DZD)", "DT (TND)",
        "NT$ (TWD)", "₾ (GEL)", "֏ (AMD)", "₼ (AZN)", "₸ (KZT)"
    )
    val symbols = listOf(
        "¥", "$", "€", "£", "A$", "C$", "CHF", "HK$",
        "S$", "NZ$", "₹", "₩", "₽", "₺", "฿", "R$",
        "S/.", "MXN", "COP$", "AR$", "CLP$", "RD$",
        "₡", "Q", "L", "C$", "B/.", "₲", "UYU", "Bs",
        "R", "₱", "RM", "Rp", "₫", "zł", "kr", "Ft",
        "Kč", "₪", "₨", "E£", "SR", "AED", "QR", "KD",
        "BD", "OMR", "JOD", "₴", "L", "лв", "din",
        "DH", "DA", "DT", "NT$", "₾", "֏", "₼", "₸"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency Symbol", color = DarkBrownText) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(symbols[index]) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSymbol == symbols[index],
                            onClick = { onSelect(symbols[index]) },
                            colors = RadioButtonDefaults.colors(selectedColor = DarkBrownText)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(option, color = Color.Black)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(4.dp)
    )
}

@Composable
fun BudgetSetupDialog(
    currentAmount: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf(currentAmount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monthly Budget", color = DarkBrownText) },
        text = {
            Column {
                Text("Set your monthly spending limit", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBrownText,
                        focusedLabelColor = DarkBrownText,
                        cursorColor = DarkBrownText
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                onConfirm(amount)
            }) {
                Text("SAVE", color = DarkBrownText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(4.dp)
    )
}

@Composable
fun PinSetupDialog(
    mode: String, 
    currentSavedPin: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var step by remember { mutableIntStateOf(if (mode == "CREATE") 1 else 0) }
    var enteredOldPin by remember { mutableStateOf("") }
    var firstNewPin by remember { mutableStateOf("") }
    var secondNewPin by remember { mutableStateOf("") }
    
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(isError) {
        if (isError) {
            delay(1200)
            isError = false
            errorMessage = ""
            if (step == 0) enteredOldPin = ""
            else if (step == 2) secondNewPin = ""
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = BlackboardBlack) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val title = when(step) {
                    0 -> "Enter Current PIN"
                    1 -> if (mode == "CHANGE") "Enter New PIN" else "Create PIN"
                    else -> "Confirm New PIN"
                }

                Text(text = title, color = ChalkWhite, fontSize = 28.sp, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(48.dp))

                val currentDisplayPin = when(step) {
                    0 -> enteredOldPin
                    1 -> firstNewPin
                    else -> secondNewPin
                }

                PinInputDisplay(pin = currentDisplayPin, isError = isError)

                if (isError) {
                    Text(text = errorMessage, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp))
                }

                Spacer(modifier = Modifier.height(64.dp))

                ChalkNumericKeypad(
                    onKeyPress = { key ->
                        if (isError) return@ChalkNumericKeypad
                        
                        when(step) {
                            0 -> {
                                if (enteredOldPin.length < 4) {
                                    enteredOldPin += key
                                    if (enteredOldPin.length == 4) {
                                        if (enteredOldPin == currentSavedPin) {
                                            if (mode == "DISABLE") onConfirm("") else step = 1
                                        } else {
                                            isError = true
                                            errorMessage = "Incorrect PIN"
                                        }
                                    }
                                }
                            }
                            1 -> {
                                if (firstNewPin.length < 4) {
                                    firstNewPin += key
                                    if (firstNewPin.length == 4) step = 2
                                }
                            }
                            2 -> {
                                if (secondNewPin.length < 4) {
                                    secondNewPin += key
                                    if (secondNewPin.length == 4) {
                                        if (firstNewPin == secondNewPin) {
                                            onConfirm(secondNewPin)
                                        } else {
                                            isError = true
                                            errorMessage = "PINs do not match"
                                            secondNewPin = ""
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onDelete = {
                        when(step) {
                            0 -> if (enteredOldPin.isNotEmpty()) enteredOldPin = enteredOldPin.dropLast(1)
                            1 -> if (firstNewPin.isNotEmpty()) firstNewPin = firstNewPin.dropLast(1)
                            2 -> if (secondNewPin.isNotEmpty()) secondNewPin = secondNewPin.dropLast(1)
                        }
                        isError = false
                    }
                )

                TextButton(onClick = onDismiss, modifier = Modifier.padding(top = 16.dp)) {
                    Text("CANCEL", color = ChalkWhite.copy(alpha = 0.6f), fontFamily = FontFamily.Cursive)
                }
            }
        }
    }
}
