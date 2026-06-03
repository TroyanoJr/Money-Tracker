package net.micode.spendingtracker.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
                settingsManager.setIncludeIncomeInBudget(accountId, it)
            }
        }
    }

    if (showBudgetDialog) {
        BudgetAmountDialog(
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
