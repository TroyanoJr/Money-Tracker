package net.micode.moneytracker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.moneytracker.util.SettingsManager
import net.micode.moneytracker.R

/**
 * Section for User Interface settings (Currency, etc).
 * Localized using string resources.
 */
@Composable
fun InterfaceSection(
    settingsManager: SettingsManager,
    onCurrencyChanged: () -> Unit
) {
    var currentCurrency by remember { mutableStateOf(settingsManager.getCurrencySymbol()) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    SettingsSectionHeader(stringResource(R.string.user_interface))
    
    SettingsClickableRow(
        title = stringResource(R.string.currency_symbol),
        value = currentCurrency
    ) { 
        showCurrencyDialog = true 
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
fun CurrencySelectionDialog(
    currentSymbol: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val commonSymbols = listOf("$", "€", "£", "¥", "₹", "₩", "₽", "฿", "₫", "₪", "₱", "₡", "₲", "₵", "₸", "₺", "₦", "₴", "SR", "RM", "RP", "Kr", "R$", "L")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_currency)) },
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
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(if (isSelected) Color(0xFF1976D2).copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                            .border(1.dp, if (isSelected) Color(0xFF1976D2) else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { onSelect(symbol) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(symbol, fontSize = 18.sp, color = if (isSelected) Color(0xFF1976D2) else Color.Black)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { 
            TextButton(onClick = onDismiss) { 
                Text(stringResource(R.string.close), color = Color.Gray) 
            } 
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    )
}
