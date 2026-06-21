package net.micode.moneytracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.moneytracker.ui.components.ChalkButton
import net.micode.moneytracker.ui.components.DottedDivider
import net.micode.moneytracker.ui.theme.*
import net.micode.moneytracker.viewmodel.AccountViewModel
import net.micode.moneytracker.util.Period
import net.micode.moneytracker.viewmodel.TransactionViewModel
import java.util.*
import kotlin.math.abs
import net.micode.moneytracker.R
/**
 * Main Spending Dashboard screen. 
 * Refactored using SRP by decomposing the UI into specialized sub-composables.
 */
@Composable
fun SpendingScreen(
    viewModel: TransactionViewModel,
    accountViewModel: AccountViewModel,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onSwitchAccountClick: () -> Unit
) {
    // Financial Data Collection
    val periodIncome by viewModel.periodIncome.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val carryOverAmount by viewModel.carryOverAmount.collectAsState()
    val dynamicBudget by viewModel.dynamicBudget.collectAsState()
    val proportionalBudget by viewModel.proportionalBudget.collectAsState()
    val expensesByCategory by viewModel.expensesByCategory.collectAsState()
    val heatmapData by viewModel.heatmapData.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    
    // UI State & Preferences
    val isBudgetEnabled by viewModel.isBudgetModeEnabled.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val isIncludeIncomeEnabled by viewModel.isIncludeIncomeEnabled.collectAsState()
    val isCarryOverEnabled by viewModel.isCarryOverEnabled.collectAsState()
    val isCarryOverAddToIncome by viewModel.isCarryOverAddToIncome.collectAsState()

    var showHeatmap by rememberSaveable { mutableStateOf(false) }

    val accounts by accountViewModel.allAccounts.collectAsState()
    val selectedAccountId by viewModel.selectedAccountId.collectAsState()

    val currentAccountName = remember(selectedAccountId, accounts) {
        if (selectedAccountId == -1L && accounts.size > 1) "All Accounts"
        else accounts.find { it.id == selectedAccountId }?.name ?: ""
    }

    LaunchedEffect(isBudgetEnabled, monthlyBudget, isIncludeIncomeEnabled, isCarryOverEnabled, isCarryOverAddToIncome) {
        viewModel.refreshBudgetSettings()
    }

    Column(modifier = Modifier.fillMaxSize().background(BlackboardBlack)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Period & Account Navigation
            PeriodSelector(
                currentAccountName = currentAccountName,
                isMultiAccount = accounts.size > 1,
                onPrevious = { viewModel.previousPeriod() },
                onNext = { viewModel.nextPeriod() },
                onAccountClick = onSwitchAccountClick
            )

            // 3. Visual Spending Progress - Now dynamic for all periods when budget is on
            SpendingProgressBar(
                isBudgetEnabled = isBudgetEnabled,
                isIncludeIncome = isIncludeIncomeEnabled,
                monthlyBudget = if (isCarryOverEnabled) dynamicBudget else proportionalBudget,
                totalIncome = totalIncome,
                totalExpense = totalExpense
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Detailed Financial Summary List
            FinancialSummarySection(
                modifier = Modifier.weight(1f),
                currencySymbol = currencySymbol,
                isBudgetEnabled = isBudgetEnabled,
                selectedPeriod = selectedPeriod,
                monthlyBudget = if (isCarryOverEnabled) dynamicBudget else proportionalBudget,
                periodIncome = periodIncome,
                totalExpense = totalExpense,
                balance = balance,
                carryOverAmount = carryOverAmount,
                expensesByCategory = expensesByCategory,
                isCarryOverEnabled = isCarryOverEnabled,
                isCarryOverAddToIncome = isCarryOverAddToIncome,
                isIncludeIncome = isIncludeIncomeEnabled
            )

            // 5. Heatmap Toggle
            Text(
                text = stringResource(R.string.show_heatmap),
                color = ChalkWhite, fontSize = 14.sp,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clickable { showHeatmap = true }
                    .border(1.dp, ChalkWhite.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )

            // 6. Primary Action Buttons
            ActionFooter(onAddExpense = onAddExpense, onAddIncome = onAddIncome)
        }
    }

    if (showHeatmap) {
        HeatmapDialog(data = heatmapData, period = selectedPeriod, onDismiss = { showHeatmap = false })
    }
}

@Composable
private fun PeriodSelector(
    currentAccountName: String,
    isMultiAccount: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onAccountClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "< ", color = ChalkWhite, fontSize = 20.sp,
            modifier = Modifier.clickable { onPrevious() }.padding(horizontal = 16.dp)
        )

        if (isMultiAccount) {
            Text(
                text = currentAccountName, color = ChalkWhite, fontSize = 18.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onAccountClick() }
            )
        } else {
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(
            text = " >", color = ChalkWhite, fontSize = 20.sp,
            modifier = Modifier.clickable { onNext() }.padding(horizontal = 16.dp)
        )
    }
}


@Composable
private fun SpendingProgressBar(
    isBudgetEnabled: Boolean,
    isIncludeIncome: Boolean,
    monthlyBudget: Double,
    totalIncome: Double,
    totalExpense: Double
) {
    val totalLimit = if (isIncludeIncome) monthlyBudget + totalIncome else monthlyBudget
    Row(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))) {
        if (isBudgetEnabled) {
            if (totalExpense <= totalLimit) {
                val spentWeight = if (totalLimit > 0) (totalExpense / totalLimit).toFloat().coerceIn(0f, 1f) else 0f
                Box(modifier = Modifier.weight((1f - spentWeight).coerceAtLeast(0.0001f)).fillMaxHeight().background(ChalkGreen))
                Box(modifier = Modifier.weight(spentWeight.coerceAtLeast(0.0001f)).fillMaxHeight().background(ChalkRed))
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(ChalkRed))
            }
        } else {
            val total = totalIncome + totalExpense
            val incomeWeight = if (total > 0) (totalIncome / total).toFloat() else 0.5f
            if (incomeWeight > 0) Box(modifier = Modifier.weight(incomeWeight.coerceAtLeast(0.0001f)).fillMaxHeight().background(ChalkGreen))
            if (1f - incomeWeight > 0) Box(modifier = Modifier.weight((1f - incomeWeight).coerceAtLeast(0.0001f)).fillMaxHeight().background(ChalkRed))
        }
    }
}

@Composable
private fun FinancialSummarySection(
    modifier: Modifier = Modifier,
    currencySymbol: String,
    isBudgetEnabled: Boolean,
    selectedPeriod: Period,
    monthlyBudget: Double,
    periodIncome: Double,
    totalExpense: Double,
    balance: Double,
    carryOverAmount: Double,
    expensesByCategory: List<Pair<String, Double>>,
    isCarryOverEnabled: Boolean,
    isCarryOverAddToIncome: Boolean,
    isIncludeIncome: Boolean
) {
    val periodLabelPrefix = when(selectedPeriod) {
        Period.DAY -> stringResource(R.string.daily)
        Period.WEEK -> stringResource(R.string.weekly)
        Period.YEAR -> stringResource(R.string.yearly)
        else -> stringResource(R.string.monthly)
    }

    LazyColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isBudgetEnabled) {
            item { BalanceRow("$periodLabelPrefix ${stringResource(R.string.budget_mode)}", String.format(Locale.getDefault(), "%s %.2f", currencySymbol, monthlyBudget), ChalkGreen) }
            if (isCarryOverEnabled && carryOverAmount != 0.0) {
                item { CarryOverItem(currencySymbol, carryOverAmount) }
            }
            if (isIncludeIncome) {
                item { BalanceRow(stringResource(R.string.income), String.format(Locale.getDefault(), "%s %.2f", currencySymbol, periodIncome), ChalkGreen) }
            }
        } else {
            item { BalanceRow(stringResource(R.string.income), String.format(Locale.getDefault(), "%s %.2f", currencySymbol, periodIncome), ChalkGreen) }
            if (isCarryOverEnabled && isCarryOverAddToIncome && carryOverAmount != 0.0) {
                item { CarryOverItem(currencySymbol, carryOverAmount) }
            }
        }

        item { BalanceRow(stringResource(R.string.expense), String.format(Locale.getDefault(), "%s %.2f", currencySymbol, totalExpense), ChalkRed) }
        
        items(expensesByCategory) { (name, amount) ->
            Row(modifier = Modifier.fillMaxWidth().padding(start = 32.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, color = ChalkWhite, fontSize = 15.sp)
                Text(String.format(Locale.getDefault(), "%s %.2f", currencySymbol, amount), color = ChalkWhite, fontSize = 15.sp)
            }
        }

        if (!isBudgetEnabled && isCarryOverEnabled && !isCarryOverAddToIncome && carryOverAmount != 0.0) {
            item {
                val sign = if (carryOverAmount < 0) "- " else ""
                BalanceRow(stringResource(R.string.carry_over), String.format(Locale.getDefault(), "%s%s %.2f", sign, currencySymbol, abs(carryOverAmount)), ChalkOrange)
            }
        }

        item {
            DottedDivider(modifier = Modifier.padding(vertical = 16.dp))
            val balanceLabel = if (isBudgetEnabled) {
                if (balance >= 0) stringResource(R.string.remaining) else stringResource(R.string.over_spending)
            } else stringResource(R.string.balance)
            
            val sign = if (balance < 0) "- " else if (balance > 0) "+ " else ""
            BalanceRow(balanceLabel, String.format(Locale.getDefault(), "%s%s %.2f", sign, currencySymbol, abs(balance)), ChalkBlue)
        }
    }
}

@Composable
private fun CarryOverItem(currencySymbol: String, amount: Double) {
    val sign = if (amount < 0) "- " else "+ "
    Row(modifier = Modifier.fillMaxWidth().padding(start = 32.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = stringResource(R.string.carry_over), color = ChalkWhite, fontSize = 15.sp)
        Text(text = String.format(Locale.getDefault(), "%s%s %.2f", sign, currencySymbol, abs(amount)), color = ChalkWhite, fontSize = 15.sp)
    }
}

@Composable
private fun ActionFooter(onAddExpense: () -> Unit, onAddIncome: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        ChalkButton(stringResource(R.string.add_expense), onClick = onAddExpense)
        ChalkButton(stringResource(R.string.add_income), onClick = onAddIncome)
    }
}

@Composable
fun BalanceRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = ChalkWhite, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HeatmapDialog(data: Map<Long, Double>, period: Period, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close), color = ChalkWhite) } },
        title = { Text(stringResource(R.string.balance_heatmap), color = ChalkWhite, fontSize = 18.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    LegendItem(ChalkGreen, stringResource(R.string.profit))
                    LegendItem(ChalkRed, stringResource(R.string.loss))
                    LegendItem(Color.DarkGray, stringResource(R.string.neutral))
                }

                if (period == Period.YEAR) YearlyHeatmap(data) else {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                            Text(day, Modifier.weight(1f), textAlign = TextAlign.Center, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    val sortedKeys = remember(data) { data.keys.sorted() }
                    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.heightIn(max = 400.dp)) {
                        items(sortedKeys) { timestamp ->
                            val balanceVal = data[timestamp] ?: 0.0
                            val color = when { balanceVal > 0 -> ChalkGreen; balanceVal < 0 -> ChalkRed; else -> Color.DarkGray }
                            val label = Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.DAY_OF_MONTH).toString()
                            Box(modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.8f)).border(0.5.dp, ChalkWhite.copy(alpha = 0.3f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                                Text(label, color = ChalkWhite.copy(alpha = 0.9f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        containerColor = BlackboardBlack,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun YearlyHeatmap(data: Map<Long, Double>) {
    val sortedDays = remember(data) { data.keys.sorted() }
    if (sortedDays.isEmpty()) return
    val maxProfit = data.values.filter { it > 0 }.maxOrNull() ?: 1.0
    val maxLoss = data.values.filter { it < 0 }.minOrNull()?.let { abs(it) } ?: 1.0

    val weeks = remember(sortedDays) {
        val w = mutableListOf<List<Long?>>()
        var currentWeek = mutableListOf<Long?>()
        val calendar = Calendar.getInstance().apply { timeInMillis = sortedDays.first() }
        val startPadding = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
        repeat(startPadding) { currentWeek.add(null) }
        sortedDays.forEach { timestamp ->
            currentWeek.add(timestamp)
            if (currentWeek.size == 7) { w.add(currentWeek); currentWeek = mutableListOf() }
        }
        if (currentWeek.isNotEmpty()) { while (currentWeek.size < 7) currentWeek.add(null); w.add(currentWeek) }
        w
    }

    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        itemsIndexed(weeks) { index, week ->
            Column {
                for (timestamp in week) {
                    val balanceVal = timestamp?.let { data[it] } ?: 0.0
                    val color = when {
                        balanceVal > 0 -> ChalkGreen.copy(alpha = (abs(balanceVal) / maxProfit).toFloat().coerceIn(0.2f, 1f))
                        balanceVal < 0 -> ChalkRed.copy(alpha = (abs(balanceVal) / maxLoss).toFloat().coerceIn(0.2f, 1f))
                        else -> Color.DarkGray.copy(alpha = 0.3f)
                    }
                    Box(modifier = Modifier.size(10.dp).padding(1.dp).clip(RoundedCornerShape(2.dp)).background(color))
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = ChalkWhite, fontSize = 10.sp)
    }
}
