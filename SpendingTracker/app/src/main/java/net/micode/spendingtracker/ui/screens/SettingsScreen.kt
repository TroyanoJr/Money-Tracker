package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.BlackboardBlack
import net.micode.spendingtracker.ui.theme.ChalkWhite
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.util.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onCurrencyChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    
    var autoCaptureEnabled by remember { mutableStateOf(settingsManager.isAutoCaptureEnabled()) }
    var currentCurrency by remember { mutableStateOf(settingsManager.getCurrencySymbol()) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    // Security States
    var passcodeEnabled by remember { mutableStateOf(settingsManager.isPasscodeEnabled()) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var pinSetupMode by remember { mutableStateOf("CREATE") } // "CREATE", "CHANGE", "DISABLE"

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
            // Section: Spending
            SettingsSectionHeader("Spending")
            SettingsToggleRow(
                title = "Auto-capture Transactions",
                subtitle = "Detect payments from notifications",
                checked = autoCaptureEnabled,
                onCheckedChange = {
                    autoCaptureEnabled = it
                    settingsManager.setAutoCaptureEnabled(it)
                }
            )

            // Section: Security
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
                        // Si ya tiene PIN, verificar antes de desactivar
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

            // Section: User Interface
            SettingsSectionHeader("User Interface")
            SettingsClickableRow(
                title = "Currency Symbol",
                value = if (currentCurrency == "¥") "Default" else currentCurrency,
                onClick = { showCurrencyDialog = true }
            )
        }
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

@Composable
fun PinSetupDialog(
    mode: String, // "CREATE", "CHANGE", "DISABLE"
    currentSavedPin: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    // 0: Verify Old, 1: Enter New, 2: Confirm New
    var step by remember { mutableIntStateOf(if (mode == "CREATE") 1 else 0) }
    var enteredOldPin by remember { mutableStateOf("") }
    var firstNewPin by remember { mutableStateOf("") }
    var secondNewPin by remember { mutableStateOf("") }
    
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Logic to handle error display and reset
    LaunchedEffect(isError) {
        if (isError) {
            delay(1200)
            isError = false
            errorMessage = ""
            // Reset only current step pins
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
                        if (isError) return@ChalkNumericKeypad // Block while showing error
                        
                        when(step) {
                            0 -> { // Verify Old
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
                            1 -> { // Enter New
                                if (firstNewPin.length < 4) {
                                    firstNewPin += key
                                    if (firstNewPin.length == 4) step = 2
                                }
                            }
                            2 -> { // Confirm New
                                if (secondNewPin.length < 4) {
                                    secondNewPin += key
                                    if (secondNewPin.length == 4) {
                                        if (firstNewPin == secondNewPin) {
                                            onConfirm(secondNewPin)
                                        } else {
                                            isError = true
                                            errorMessage = "PINs do not match"
                                            // Reset confirmation only
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
fun SettingsToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = Color(0xFF1976D2))
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, color = if (enabled) Color(0xFF1976D2) else Color.Gray)
        Text(value, fontSize = 16.sp, color = if (enabled) Color(0xFF1976D2) else Color.Gray)
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
}

@Composable
fun CurrencySelectionDialog(currentSymbol: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val options = listOf("¥ (Default)", "$", "€", "£", "S/.", "MXN")
    val symbols = listOf("¥", "$", "€", "£", "S/.", "MXN")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency Symbol", color = DarkBrownText) },
        text = {
            Column {
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
