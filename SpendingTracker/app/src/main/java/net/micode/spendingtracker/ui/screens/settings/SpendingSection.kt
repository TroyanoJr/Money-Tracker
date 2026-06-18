package net.micode.spendingtracker.ui.screens.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import net.micode.spendingtracker.R
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.util.SettingsManager

/**
 * Section for Budget and Spending settings.
 * Individual settings per account.
 */
@Composable
fun SpendingSection(
    settingsManager: SettingsManager,
    currentCurrency: String,
    accountId: Long,
    accountName: String,
    onNavigateToTimePeriod: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToCarryOver: () -> Unit
) {
    val budgetModeEnabled = settingsManager.isBudgetModeEnabled(accountId)
    val isAllAccounts = accountId == -1L
    
    // In "All Accounts" mode, Carry Over is always displayed as "Off"
    val carryOverEnabled = if (isAllAccounts) false else settingsManager.isCarryOverEnabled(accountId)
    
    var showCarryOverError by remember { mutableStateOf(false) }

    // Header with current account name: Spending (AccountName)
    SettingsSectionHeader("${stringResource(R.string.spending_section)} ($accountName)")
    
    SettingsClickableRow(
        title = stringResource(R.string.time_period),
        value = when(settingsManager.getDefaultTimePeriod()) {
            "DAY" -> stringResource(R.string.daily)
            "WEEK" -> stringResource(R.string.weekly)
            "YEAR" -> stringResource(R.string.yearly)
            else -> stringResource(R.string.monthly)
        },
        onClick = onNavigateToTimePeriod
    )

    SettingsClickableRow(
        title = stringResource(R.string.budget_mode),
        value = if (budgetModeEnabled) stringResource(R.string.on) else stringResource(R.string.off),
        onClick = onNavigateToBudget
    )

    SettingsClickableRow(
        title = stringResource(R.string.carry_over),
        value = if (carryOverEnabled) stringResource(R.string.on) else stringResource(R.string.off),
        onClick = {
            if (isAllAccounts) {
                showCarryOverError = true
            } else {
                onNavigateToCarryOver()
            }
        }
    )

    if (showCarryOverError) {
        AlertDialog(
            onDismissRequest = { showCarryOverError = false },
            title = { Text(text = "Error", fontWeight = FontWeight.Bold) },
            text = { 
                Text(
                    text = "'Carry Over' cannot be set for 'All Accounts', only for specific Accounts.\n\nPlease switch to a specific Account and try again."
                ) 
            },
            confirmButton = {
                TextButton(onClick = { showCarryOverError = false }) {
                    Text("CLOSE", color = DarkBrownText, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun BudgetAmountDialog(
    currentAmount: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var textValue by remember { mutableStateOf(currentAmount.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.monthly_budget)) },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text("${stringResource(R.string.amount)} ($currencySymbol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { 
                val amount = textValue.toDoubleOrNull() ?: 0.0
                onConfirm(amount)
            }) {
                Text(stringResource(R.string.save), color = DarkBrownText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    )
}
