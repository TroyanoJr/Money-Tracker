package net.micode.spendingtracker.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import net.micode.spendingtracker.R
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.util.SettingsManager
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSettingsScreen(
    settingsManager: SettingsManager,
    accountId: Long,
    accountName: String,
    currentCurrency: String,
    onBack: () -> Unit
) {
    var budgetModeEnabled by remember(accountId) { mutableStateOf(settingsManager.isBudgetModeEnabled(accountId)) }
    var includeIncome by remember(accountId) { mutableStateOf(settingsManager.isIncludeIncomeInBudget(accountId)) }
    var monthlyBudget by remember(accountId) { mutableDoubleStateOf(settingsManager.getMonthlyBudget(accountId)) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    // Dynamic label prefix based on selected period
    val periodPrefix = when(settingsManager.getDefaultTimePeriod()) {
        "DAY" -> stringResource(R.string.daily)
        "WEEK" -> stringResource(R.string.weekly)
        "YEAR" -> stringResource(R.string.yearly)
        else -> stringResource(R.string.monthly)
    }
    val budgetLabel = "$periodPrefix ${stringResource(R.string.budget_mode)}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.budget_mode), color = DarkBrownText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = DarkBrownText)
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
            SettingsSectionHeader("${stringResource(R.string.spending_section)} ($accountName)")

            SettingsToggleRow(
                title = stringResource(R.string.budget_mode),
                subtitle = stringResource(R.string.budget_mode_desc),
                checked = budgetModeEnabled
            ) {
                budgetModeEnabled = it
                settingsManager.setBudgetModeEnabled(accountId, it)
            }

            SettingsClickableRow(
                title = budgetLabel,
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
                settingsManager.setIncludeIncomeInBudget(accountId, it)
            }
        }
    }

    if (showBudgetDialog) {
        BudgetAmountDialog(
            title = budgetLabel,
            currentAmount = monthlyBudget,
            currencySymbol = currentCurrency,
            onDismiss = { showBudgetDialog = false },
            onConfirm = {
                monthlyBudget = it
                settingsManager.setMonthlyBudget(accountId, it)
                showBudgetDialog = false
            }
        )
    }
}

@Composable
fun BudgetAmountDialog(
    title: String,
    currentAmount: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var textValue by remember { mutableStateOf(currentAmount.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    )
}
