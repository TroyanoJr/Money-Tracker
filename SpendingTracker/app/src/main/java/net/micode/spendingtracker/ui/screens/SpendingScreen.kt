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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import net.micode.spendingtracker.viewmodel.Period
import net.micode.spendingtracker.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SpendingScreen(
    viewModel: TransactionViewModel,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit
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

    LaunchedEffect(isBudgetEnabled, monthlyBudget, isIncludeIncomeEnabled) {
        viewModel.refreshBudgetSettings()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackboardBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousPeriod() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Period",
                        tint = ChalkWhite,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(48.dp))
                IconButton(onClick = { viewModel.nextPeriod() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Period",
                        tint = ChalkWhite,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            if (incompleteCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(1.dp, ChalkWhite.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = ChalkRed, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "You have $incompleteCount transactions pending to classify",
                        color = ChalkWhite,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Cursive
                    )
                }
            }

            // Budget Section
            AnimatedVisibility(visible = isBudgetEnabled && selectedPeriod == Period.MONTH) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    val effectiveBudget = if (isIncludeIncomeEnabled) monthlyBudget + totalIncome else monthlyBudget
                    val progress = if (effectiveBudget > 0) (totalExpense / effectiveBudget).toFloat() else 0f
                    val remaining = effectiveBudget - totalExpense
                    val progressColor = when {
                        progress >= 1.0f -> ChalkRed
                        progress >= 0.8f -> Color(0xFFFFD54F)
                        else -> ChalkWhite.copy(alpha = 0.8f)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = if (isIncludeIncomeEnabled) "Dynamic Budget" else "Monthly Budget",
                            color = ChalkWhite.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Cursive
                        )
                        Text(
                            text = "$currencySymbol ${String.format("%.2f", remaining)} left",
                            color = progressColor,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Cursive,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(ChalkWhite.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceAtMost(1f))
                                .fillMaxHeight()
                                .background(progressColor)
                        )
                    }
                }
            }

            // Proportional Balance Bar (Solid, No Gaps)
            val total = totalIncome + totalExpense
            val incomeWeight = if (total > 0) (totalIncome / total).toFloat() else 0.5f
            val expenseWeight = 1f - incomeWeight

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
            ) {
                if (incomeWeight > 0) {
                    Box(
                        modifier = Modifier
                            .weight(incomeWeight.coerceAtLeast(0.0001f))
                            .fillMaxHeight()
                            .background(ChalkGreen)
                    )
                }
                if (expenseWeight > 0) {
                    Box(
                        modifier = Modifier
                            .weight(expenseWeight.coerceAtLeast(0.0001f))
                            .fillMaxHeight()
                            .background(ChalkRed)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    BalanceRow("Income", "$currencySymbol ${String.format("%.2f", totalIncome)}", ChalkGreen)
                }
                item {
                    BalanceRow("Expense", "$currencySymbol ${String.format("%.2f", totalExpense)}", ChalkRed)
                }
                items(expensesByCategory) { (name, amount) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, top = 2.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name, color = ChalkWhite, fontSize = 20.sp, fontFamily = FontFamily.Cursive)
                        Text("$currencySymbol ${String.format("%.2f", amount)}", color = ChalkWhite, fontSize = 20.sp, fontFamily = FontFamily.Cursive)
                    }
                }
                item {
                    DottedDivider(modifier = Modifier.padding(vertical = 16.dp))
                    BalanceRow("Balance", "$currencySymbol ${String.format("%.2f", balance)}", ChalkBlue)
                }
            }

            Text(
                text = "Show Heatmap",
                color = ChalkWhite,
                fontSize = 16.sp,
                fontFamily = FontFamily.Cursive,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clickable { showHeatmap = true }
                    .border(1.dp, ChalkWhite.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChalkButton("+ Expense", onClick = onAddExpense)
                ChalkButton("+ Income", onClick = onAddIncome)
            }
            
            Text(
                "** Rotate device to view reports **",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    if (showHeatmap) {
        HeatmapDialog(
            data = heatmapData,
            period = selectedPeriod,
            onDismiss = { showHeatmap = false }
        )
    }
}

@Composable
fun HeatmapDialog(data: Map<Long, Double>, period: Period, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = ChalkWhite, fontFamily = FontFamily.Cursive)
            }
        },
        title = {
            Text("Balance Heatmap", color = ChalkWhite, fontFamily = FontFamily.Cursive, fontSize = 24.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = ChalkGreen, label = "Profit")
                    LegendItem(color = ChalkRed, label = "Loss")
                    LegendItem(color = Color.DarkGray, label = "Neutral")
                }

                if (period == Period.YEAR) {
                    YearlyHeatmap(data)
                } else {
                    if (period == Period.MONTH || period == Period.WEEK) {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                            val days = listOf("M", "T", "W", "T", "F", "S", "S")
                            days.forEach { day ->
                                Text(
                                    text = day,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        val sortedKeys = data.keys.sorted()
                        items(sortedKeys) { timestamp ->
                            val balance = data[timestamp] ?: 0.0
                            val color = when {
                                balance > 0 -> ChalkGreen
                                balance < 0 -> ChalkRed
                                else -> Color.DarkGray
                            }
                            
                            val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
                            val label = calendar.get(Calendar.DAY_OF_MONTH).toString()
                            
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color.copy(alpha = 0.8f))
                                    .border(0.5.dp, ChalkWhite.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = ChalkWhite.copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Cursive,
                                    fontWeight = FontWeight.Bold
                                )
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
    val sortedDays = data.keys.sorted()
    if (sortedDays.isEmpty()) return

    val maxProfit = data.values.filter { it > 0 }.maxOrNull() ?: 1.0
    val maxLoss = data.values.filter { it < 0 }.minOrNull()?.let { Math.abs(it) } ?: 1.0

    val weeks = mutableListOf<List<Long?>>()
    var currentWeek = mutableListOf<Long?>()
    
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = sortedDays.first()
    
    val startPadding = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
    repeat(startPadding) { currentWeek.add(null) }
    
    sortedDays.forEach { timestamp ->
        currentWeek.add(timestamp)
        if (currentWeek.size == 7) {
            weeks.add(currentWeek)
            currentWeek = mutableListOf()
        }
    }
    if (currentWeek.isNotEmpty()) {
        while (currentWeek.size < 7) currentWeek.add(null)
        weeks.add(currentWeek)
    }

    val daysOfWeekLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(top = 22.dp, end = 8.dp)) {
                    daysOfWeekLabels.forEachIndexed { index, day ->
                        Box(modifier = Modifier.size(12.dp), contentAlignment = Alignment.Center) {
                            if (index == 0 || index == 2 || index == 4 || index == 6) { 
                                Text(day, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }
            }

            itemsIndexed(weeks) { index, week ->
                val firstNonNullDay = week.find { it != null }
                var monthLabel = ""
                var isNewMonth = false
                
                if (firstNonNullDay != null) {
                    val cal = Calendar.getInstance().apply { timeInMillis = firstNonNullDay }
                    if (cal.get(Calendar.DAY_OF_MONTH) <= 7) {
                        monthLabel = SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)
                        isNewMonth = true
                    }
                }

                Row {
                    if (isNewMonth && index > 0) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(115.dp)
                                .padding(top = 22.dp)
                                .background(ChalkWhite.copy(alpha = 0.2f))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Column {
                        Text(
                            text = monthLabel,
                            color = ChalkWhite.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            modifier = Modifier.height(22.dp),
                            maxLines = 1,
                            fontFamily = FontFamily.Cursive
                        )

                        week.forEach { timestamp ->
                            if (timestamp == null) {
                                Box(modifier = Modifier.size(12.dp))
                            } else {
                                val balance = data[timestamp] ?: 0.0
                                val color = when {
                                    balance > 0 -> {
                                        val intensity = (balance / maxProfit).coerceIn(0.0, 1.0)
                                        ChalkGreen.copy(alpha = (0.3f + intensity * 0.7f).toFloat())
                                    }
                                    balance < 0 -> {
                                        val intensity = (Math.abs(balance) / maxLoss).coerceIn(0.0, 1.0)
                                        ChalkRed.copy(alpha = (0.3f + intensity * 0.7f).toFloat())
                                    }
                                    else -> Color.DarkGray.copy(alpha = 0.2f)
                                }
                                
                                val isStartOrEnd = timestamp == sortedDays.first() || timestamp == sortedDays.last()

                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(color)
                                        .then(
                                            if (isStartOrEnd) Modifier.border(1.dp, ChalkWhite, RoundedCornerShape(2.dp))
                                            else Modifier
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                }
            }
        }
        
        if (sortedDays.isNotEmpty()) {
            val startStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(sortedDays.first()))
            val endStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(sortedDays.last()))
            Text(
                text = "$startStr - $endStr",
                color = Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Cursive,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, color = ChalkWhite, fontSize = 12.sp, fontFamily = FontFamily.Cursive)
    }
}

@Composable
fun BalanceRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = ChalkWhite, fontSize = 28.sp, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 28.sp, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
    }
}
