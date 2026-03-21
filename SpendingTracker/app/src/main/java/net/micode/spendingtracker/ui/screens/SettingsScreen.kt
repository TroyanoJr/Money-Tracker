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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.util.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    
    var autoCaptureEnabled by remember { mutableStateOf(settingsManager.isAutoCaptureEnabled()) }
    var currentCurrency by remember { mutableStateOf(settingsManager.getCurrencySymbol()) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

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
            }
        )
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
fun SettingsClickableRow(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, color = Color(0xFF1976D2))
        Text(value, fontSize = 16.sp, color = Color(0xFF1976D2))
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
