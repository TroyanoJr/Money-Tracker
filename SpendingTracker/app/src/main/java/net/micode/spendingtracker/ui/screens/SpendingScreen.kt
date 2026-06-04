package net.micode.spendingtracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import net.micode.spendingtracker.R
import net.micode.spendingtracker.ui.components.ChalkButton
import net.micode.spendingtracker.ui.components.DottedDivider
import net.micode.spendingtracker.ui.theme.*
import net.micode.spendingtracker.viewmodel.AccountViewModel
import net.micode.spendingtracker.viewmodel.Period
import net.micode.spendingtracker.viewmodel.TransactionViewModel
import java.util.*
import kotlin.math.abs

/**
 * Main dashboard screen displaying the financial summary for a selected period.
 * It follows a "Blackboard" aesthetic with chalk-like colors.
 */
@Composable
fun SpendingScreen(
    viewModel: TransactionViewModel,
    accountViewModel: AccountViewModel,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onSwitchAccountClick: () -> Unit
) {
    // Collect state flows from ViewModel as Compose state
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val carryOverAmount by viewModel.carryOverAmount.collectAsState()
    val dynamicBudget by viewModel.dynamicBudget.collectAsState()
    val expensesByCategory by viewModel.expensesByCategory.collectAsState()
    val heatmapData by viewModel.heatmapData.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    
    // User preferences for financial logic
    val isBudgetEnabled by viewModel.isBudgetModeEnabled.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val isIncludeIncomeEnabled by viewModel.isIncludeIncomeEnabled.collectAsState()
    val isCarryOverEnabled by viewModel.isCarryOverEnabled.collectAsState()
    val isCarryOverAddToIncome by viewModel.isCarryOverAddToIncome.collectAsState()

    val incompleteCount by viewModel.incompleteTransactionsCount.collectAsState()
    var showHeatmap by rememberSaveable { mutableStateOf(false) }

    val accounts by accountViewModel.allAccounts.collectAsState()
    val selectedAccountId by viewModel.selectedAccountId.collectAsState()

    // Determine displayed account name
    val currentAccountName = remember(selectedAccountId, accounts) {
        if (selectedAccountId == -1L && accounts.size > 1) "All Accounts"
        else accounts.find { it.id == selectedAccountId }?.name ?: "Default"
    }

    // Refresh settings whenever account or core preferences change
    LaunchedEffect(isBudgetEnabled, monthlyBudget, isIncludeIncomeEnabled, isCarryOverEnabled, isCarryOverAddToIncome) {
        viewModel.refreshBudgetSettings()
    }

    Column(modifier = Modifier.fillMaxSize().background(BlackboardBlack)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Period navigation header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "< ",
                    color = ChalkWhite,
                    fontSize = 20.sp,
                    modifier = Modifier.clickable { viewModel.previousPeriod() }.padding(horizontal = 16.dp)
                )

                Text(
                    text = currentAccountName,
                    color = ChalkWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSwitchAccountClick() }
                )
                
                Text(
                    text = " >",
                    color = ChalkWhite,
                    fontSize = 20.sp,
                    modifier = Modifier.clickable { viewModel.nextPeriod() }.padding(horizontal = 16.dp)
                )
            }

            // Warning bar for pending (incomplete) transactions
            if (incompleteCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).border(1.dp, ChalkWhite.copy(alpha = 0.4f), RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.05f)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = ChalkRed, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.pending_transactions, incompleteCount), color = ChalkWhite, fontSize = 12.sp)
                }
            }

            /**
             * Visual Progress Bar
             * Logic: 
             * - In Budget Mode: Green bar shrinks as red bar (expenses) grows towards the limit.
             * - In Standard Mode: Shows relative weight of Income (Green) vs Expense (Red).
             */
            val totalLimit = if (isIncludeIncomeEnabled) dynamicBudget + totalIncome else dynamicBudget
            Row(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))) {
                if (isBudgetEnabled && selectedPeriod == Period.MONTH && selectedAccountId != -1L) {
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

            Spacer(modifier = Modifier.height(24.dp))

            // Main financial summary list
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                
                // INCOME SECTION - Shows monthly income or budget limit
                if (isBudgetEnabled && selectedPeriod == Period.MONTH && selectedAccountId != -1L) {
                    item { 
                        val budgetLabel = stringResource(R.string.monthly_budget)
                        val budgetToShow = if (isCarryOverEnabled) dynamicBudget else monthlyBudget
                        BalanceRow(budgetLabel, String.format(Locale.getDefault(), "%s %.2f", currencySymbol, budgetToShow), ChalkGreen) 
                    }
                    
                    /**
                     * CARRY OVER SUB-ITEM (Budget Mode)
                     * Displays Carry Over as a detailed breakdown under Budget when active.
                     */
                    if (isCarryOverEnabled && carryOverAmount != 0.0) {
                        item {
                            val sign = if (carryOverAmount < 0) "- " else "+ "
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.carry_over),
                                    color = ChalkWhite,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%s%s %.2f", sign, currencySymbol, abs(carryOverAmount)),
                                    color = ChalkWhite,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    if (isIncludeIncomeEnabled) {
                        item { BalanceRow(stringResource(R.string.income), String.format(Locale.getDefault(), "%s %.2f", currencySymbol, totalIncome), ChalkGreen) }
                    }
                } else {
                    item { BalanceRow(stringResource(R.string.income), String.format(Locale.getDefault(), "%s %.2f", currencySymbol, totalIncome), ChalkGreen) }
                    
                    /**
                     * CARRY OVER SUB-ITEM
                     * If 'Add to Income' is enabled, display Carry Over as a detailed breakdown under Income.
                     * Using ChalkWhite color and indentation to match the itemized expense style.
                     */
                    if (isCarryOverEnabled && isCarryOverAddToIncome && carryOverAmount != 0.0) {
                        item {
                            val sign = if (carryOverAmount < 0) "- " else "+ "
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.carry_over),
                                    color = ChalkWhite,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%s%s %.2f", sign, currencySymbol, abs(carryOverAmount)),
                                    color = ChalkWhite,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // EXPENSE ROW - Always red
                item { BalanceRow(stringResource(R.string.expense), String.format(Locale.getDefault(), "%s %.2f", currencySymbol, totalExpense), ChalkRed) }
                
                // DETAILED EXPENSES - Itemized by category
                items(expensesByCategory) { (name, amount) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(start = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name, color = ChalkWhite, fontSize = 14.sp)
                        Text(String.format(Locale.getDefault(), "%s %.2f", currencySymbol, amount), color = ChalkWhite, fontSize = 14.sp)
                    }
                }

                /**
                 * STANDALONE CARRY OVER ROW
                 * Only displayed if Carry Over is NOT integrated into the Income total and Budget is disabled.
                 * Uses ChalkOrange for high visibility.
                 */
                if (!isBudgetEnabled && isCarryOverEnabled && !isCarryOverAddToIncome && carryOverAmount != 0.0) {
                    item {
                        val sign = if (carryOverAmount < 0) "- " else ""
                        val formattedValue = String.format(Locale.getDefault(), "%s%s %.2f", sign, currencySymbol, abs(carryOverAmount))
                        BalanceRow(stringResource(R.string.carry_over), formattedValue, ChalkOrange)
                    }
                }

                /**
                 * FINAL BALANCE SECTION
                 * Displays net cumulative profit/loss or remaining budget.
                 */
                item {
                    DottedDivider(modifier = Modifier.padding(vertical = 16.dp))
                    val balanceLabel = if (isBudgetEnabled) {
                        if (balance >= 0) stringResource(R.string.remaining) else stringResource(R.string.over_spending)
                    } else {
                        stringResource(R.string.balance)
                    }
                    
                    // Display '+' for positive balance and '-' for negative to match user reference images
                    val sign = if (balance < 0) "- " else if (balance > 0) "+ " else ""
                    val formattedValue = String.format(Locale.getDefault(), "%s%s %.2f", sign, currencySymbol, abs(balance))
                    BalanceRow(balanceLabel, formattedValue, ChalkBlue)
                }
            }

            // Secondary actions and heatmap entry
            Text(
                text = stringResource(R.string.show_heatmap),
                color = ChalkWhite, fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp).clickable { showHeatmap = true }.border(1.dp, ChalkWhite.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
            )

            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                ChalkButton(stringResource(R.string.add_expense), onClick = onAddExpense)
                ChalkButton(stringResource(R.string.add_income), onClick = onAddIncome)
            }

            Text(text = stringResource(R.string.rotate_device_hint), color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }

    if (showHeatmap) {
        HeatmapDialog(data = heatmapData, period = selectedPeriod, onDismiss = { showHeatmap = false })
    }
}

/**
 * Dialog displaying a grid/calendar heatmap of financial activity.
 */
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

/**
 * Compact horizontal heatmap representation for the yearly view.
 */
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
        itemsIndexed(weeks) { _, week ->
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

@Composable
fun BalanceRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = ChalkWhite, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
