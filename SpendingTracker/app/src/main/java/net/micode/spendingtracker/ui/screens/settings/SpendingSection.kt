package net.micode.spendingtracker.ui.screens.settings

import androidx.compose.foundation.layout.fillMaxWidth
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
import java.util.Locale

/**
 * Section for Budget and Spending settings.
 * Fully localized using string resources.
 */
@Composable
fun SpendingSection(
    settingsManager: SettingsManager,
    currentCurrency: String
) {
    var budgetModeEnabled by remember { mutableStateOf(settingsManager.isBudgetModeEnabled()) }
    var includeIncome by remember { mutableStateOf(settingsManager.isIncludeIncomeInBudget()) }
    var monthlyBudget by remember { mutableDoubleStateOf(settingsManager.getMonthlyBudget()) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    SettingsSectionHeader(stringResource(R.string.spending_section))
    
    SettingsToggleRow(
        title = stringResource(R.string.budget_mode),
        subtitle = stringResource(R.string.budget_mode_desc),
        checked = budgetModeEnabled
    ) { 
        budgetModeEnabled = it
        settingsManager.setBudgetModeEnabled(it) 
    }

    SettingsClickableRow(
        title = stringResource(R.string.monthly_budget),
        value = String.format(Locale.getDefault(), "%s %.2f", currentCurrency, monthlyBudget),
        enabled = budgetModeEnabled
    ) { 
        showBudgetDialog = true
    }

    SettingsToggleRow(
        title = stringResource(R.string.include_income),
        subtitle = stringResource(R.string.include_income_desc),
        checked = includeIncome,
        enabled = budgetModeEnabled
    ) { 
        includeIncome = it
        settingsManager.setIncludeIncomeInBudget(it) 
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
