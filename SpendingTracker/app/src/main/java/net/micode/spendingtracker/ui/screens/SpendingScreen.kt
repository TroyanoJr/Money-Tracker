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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
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

@Composable
fun SpendingScreen(
    viewModel: TransactionViewModel,
    accountViewModel: AccountViewModel,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onSwitchAccountClick: () -> Unit
) {
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val expensesByCategory by viewModel.expensesByCategory.collectAsState()
    val heatmapData by viewModel.heatmapData.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    
    val isBudgetEnabled by viewModel.isBudgetModeEnabled.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val isIncludeIncomeEnabled by viewModel.isIncludeIncomeEnabled.collectAsState()

    val incompleteCount by viewModel.incompleteTransactionsCount.collectAsState()
    var showHeatmap by rememberSaveable { mutableStateOf(false) }

    val accounts by accountViewModel.allAccounts.collectAsState()
    val selectedAccountId by viewModel.selectedAccountId.collectAsState()
    
    val currentAccount = remember(selectedAccountId, accounts) {
        accounts.find { it.id == selectedAccountId }
    }
    
    val currentAccountName = remember(selectedAccountId, accounts) {
        if (selectedAccountId == -1L) "All Accounts"
        else currentAccount?.name ?: "Default"
    }

    val cycleAccount = { direction: Int ->
        val list = listOf(-1L) + accounts.map { it.id }
        val currentIndex = list.indexOf(selectedAccountId)
        if (currentIndex != -1) {
            val nextIndex = (currentIndex + direction + list.size) % list.size
            viewModel.setSelectedAccount(list[nextIndex])
        }
    }

    LaunchedEffect(isBudgetEnabled, monthlyBudget, isIncludeIncomeEnabled) {
        viewModel.refreshBudgetSettings()
    }

    Column(modifier = Modifier.fillMaxSize().background(BlackboardBlack)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Account selection header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "< ", 
                    color = ChalkWhite, 
                    fontSize = 24.sp, 
                    fontFamily = FontFamily.Cursive,
                    modifier = Modifier.clickable { cycleAccount(-1) }.padding(horizontal = 16.dp)
                )
                
                // Added Icon with Account Color
                if (selectedAccountId != -1L) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = if (currentAccount != null) Color(currentAccount.color) else ChalkWhite,
                        modifier = Modifier.size(28.dp).padding(end = 8.dp)
                    )
                }

                Text(
                    text = currentAccountName,
                    color = ChalkWhite,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSwitchAccountClick() }
                )
                Text(
                    text = " >", 
                    color = ChalkWhite, 
                    fontSize = 24.sp, 
                    fontFamily = FontFamily.Cursive,
                    modifier = Modifier.clickable { cycleAccount(1) }.padding(horizontal = 16.dp)
                )
            }

            if (incompleteCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).border(1.dp, ChalkWhite.copy(alpha = 0.4f), RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.05f)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = ChalkRed, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.pending_transactions, incompleteCount), color = ChalkWhite, fontSize = 14.sp, fontFamily = FontFamily.Cursive)
                }
            }

            // Budget visibility fix
            AnimatedVisibility(visible = isBudgetEnabled && selectedPeriod == Period.MONTH && selectedAccountId != -1L) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    val effectiveBudget = if (isIncludeIncomeEnabled) monthlyBudget + totalIncome else monthlyBudget
                    val progress = if (effectiveBudget > 0) (totalExpense / effectiveBudget).toFloat() else 0f
                    val remaining = effectiveBudget - totalExpense
                    val progressColor = when {
                        progress >= 1.0f -> ChalkRed
                        progress >= 0.8f -> Color(0xFFFFD54F)
                        else -> ChalkWhite.copy(alpha = 0.8f)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text(text = if (isIncludeIncomeEnabled) stringResource(R.string.dynamic_budget) else stringResource(R.string.monthly_budget), color = ChalkWhite.copy(alpha = 0.6f), fontSize = 14.sp, fontFamily = FontFamily.Cursive)
                        // Correct formatting fix
                        Text(text = stringResource(R.string.amount_left, currencySymbol, remaining), color = progressColor, fontSize = 16.sp, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(ChalkWhite.copy(alpha = 0.1f))) {
                        Box(modifier = Modifier.fillMaxWidth(progress.coerceAtMost(1f)).fillMaxHeight().background(progressColor))
                    }
                }
            }

            val total = totalIncome + totalExpense
            val incomeWeight = if (total > 0) (totalIncome / total).toFloat() else 0.5f
            Row(modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp))) {
                if (incomeWeight > 0) Box(modifier = Modifier.weight(incomeWeight.coerceAtLeast(0.0001f)).fillMaxHeight().background(ChalkGreen))
                if (1f - incomeWeight > 0) Box(modifier = Modifier.weight((1f - incomeWeight).coerceAtLeast(0.0001f)).fillMaxHeight().background(ChalkRed))
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { BalanceRow(stringResource(R.string.income), String.format(Locale.getDefault(), "%s %.2f", currencySymbol, totalIncome), ChalkGreen) }
                item { BalanceRow(stringResource(R.string.expense), String.format(Locale.getDefault(), "%s %.2f", currencySymbol, totalExpense), ChalkRed) }
                items(expensesByCategory) { (name, amount) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(start = 32.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name, color = ChalkWhite, fontSize = 20.sp, fontFamily = FontFamily.Cursive)
                        Text(String.format(Locale.getDefault(), "%s %.2f", currencySymbol, amount), color = ChalkWhite, fontSize = 20.sp, fontFamily = FontFamily.Cursive)
                    }
                }
                item {
                    DottedDivider(modifier = Modifier.padding(vertical = 16.dp))
                    BalanceRow(stringResource(R.string.balance), String.format(Locale.getDefault(), "%s %.2f", currencySymbol, balance), ChalkBlue)
                }
            }

            Text(
                text = stringResource(R.string.show_heatmap),
                color = ChalkWhite, fontSize = 16.sp, fontFamily = FontFamily.Cursive,
                modifier = Modifier.padding(bottom = 8.dp).clickable { showHeatmap = true }.border(1.dp, ChalkWhite.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                ChalkButton(stringResource(R.string.add_expense), onClick = onAddExpense)
                ChalkButton(stringResource(R.string.add_income), onClick = onAddIncome)
            }
            
            Text(text = stringResource(R.string.rotate_device_hint), color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }

    if (showHeatmap) {
        HeatmapDialog(data = heatmapData, period = selectedPeriod, onDismiss = { showHeatmap = false })
    }
}

@Composable
fun HeatmapDialog(data: Map<Long, Double>, period: Period, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close), color = ChalkWhite, fontFamily = FontFamily.Cursive) } },
        title = { Text(stringResource(R.string.balance_heatmap), color = ChalkWhite, fontFamily = FontFamily.Cursive, fontSize = 24.sp) },
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
                            Text(day, Modifier.weight(1f), textAlign = TextAlign.Center, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    val sortedKeys = remember(data) { data.keys.sorted() }
                    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.heightIn(max = 400.dp)) {
                        items(sortedKeys) { timestamp ->
                            val balanceVal = data[timestamp] ?: 0.0
                            val color = when { balanceVal > 0 -> ChalkGreen; balanceVal < 0 -> ChalkRed; else -> Color.DarkGray }
                            val label = Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.DAY_OF_MONTH).toString()
                            Box(modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.8f)).border(0.5.dp, ChalkWhite.copy(alpha = 0.3f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                                Text(label, color = ChalkWhite.copy(alpha = 0.9f), fontSize = 10.sp, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
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
        itemsIndexed(weeks) { _, week ->
            Column {
                for (timestamp in week) {
                    if (timestamp == null) {
                        Spacer(Modifier.size(12.dp))
                    } else {
                        val balanceVal = data[timestamp] ?: 0.0
                        val color = when {
                            balanceVal > 0 -> ChalkGreen.copy(alpha = (0.3f + (balanceVal / maxProfit) * 0.7f).toFloat())
                            balanceVal < 0 -> ChalkRed.copy(alpha = (0.3f + (abs(balanceVal) / maxLoss) * 0.7f).toFloat())
                            else -> Color.DarkGray.copy(alpha = 0.2f)
                        }
                        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(color))
                    }
                    Spacer(Modifier.height(3.dp))
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, color = ChalkWhite, fontSize = 12.sp, fontFamily = FontFamily.Cursive)
    }
}

@Composable
fun BalanceRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = ChalkWhite, fontSize = 28.sp, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 28.sp, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
    }
}
