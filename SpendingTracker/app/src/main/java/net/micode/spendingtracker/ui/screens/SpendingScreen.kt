package net.micode.spendingtracker.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.ui.components.ChalkButton
import net.micode.spendingtracker.ui.components.DottedDivider
import net.micode.spendingtracker.ui.theme.*
import net.micode.spendingtracker.viewmodel.Period
import net.micode.spendingtracker.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen that shows the financial overview (Blackboard style).
 * Shows Total Income, Total Expenses with category breakdown, and the final Balance.
 */
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
    
    // Incomplete transactions data
    val incompleteCount by viewModel.incompleteTransactionsCount.collectAsState()
    
    var showHeatmap by remember { mutableStateOf(false) }

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
            // Period Navigation Arrows
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Period",
                    tint = ChalkWhite,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { viewModel.previousPeriod() }
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.width(48.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Period",
                    tint = ChalkWhite,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { viewModel.nextPeriod() }
                        .padding(4.dp)
                )
            }

            // Incomplete Transactions Notice
            if (incompleteCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(1.dp, ChalkWhite.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { /* Logic to open pending list later */ }
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

            // Visual balance bar
            val total = totalIncome + totalExpense
            val incomeWeight = if (total > 0) (totalIncome / total).toFloat() else 0.5f
            val expenseWeight = if (total > 0) (totalExpense / total).toFloat() else 0.5f

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                if (total > 0) {
                    Box(modifier = Modifier.weight(incomeWeight.coerceAtLeast(0.01f)).fillMaxHeight().background(ChalkGreen))
                    Box(modifier = Modifier.weight(expenseWeight.coerceAtLeast(0.01f)).fillMaxHeight().background(ChalkRed))
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.3f)))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Scrollable list for the blackboard content
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    BalanceRow("Income", "¥ ${String.format("%.2f", totalIncome)}", ChalkGreen)
                }
                item {
                    BalanceRow("Expense", "¥ ${String.format("%.2f", totalExpense)}", ChalkRed)
                }
                items(expensesByCategory) { (name, amount) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, top = 2.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name, color = ChalkWhite, fontSize = 20.sp, fontFamily = FontFamily.Cursive)
                        Text("¥ ${String.format("%.2f", amount)}", color = ChalkWhite, fontSize = 20.sp, fontFamily = FontFamily.Cursive)
                    }
                }
                item {
                    DottedDivider(modifier = Modifier.padding(vertical = 16.dp))
                    BalanceRow("Balance", "¥ ${String.format("%.2f", balance)}", ChalkBlue)
                }
            }

            // Heatmap Button
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

            // Action buttons
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
                // Visual Legend
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
                        // Day of week initials
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
    
    // Iniciar siempre en Lunes para consistencia visual (Estilo GitHub)
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
            // Etiquetas de días a la izquierda
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
                    // Si el día 1 del mes cae en esta semana, ponemos la etiqueta
                    if (cal.get(Calendar.DAY_OF_MONTH) <= 7) {
                        monthLabel = SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)
                        isNewMonth = true
                    }
                }

                Row {
                    // Separador vertical sutil al inicio de cada mes
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
                        // Nombre del mes centrado sobre la semana
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
                                
                                // Borde especial para el primer y último día del rango
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
        
        // Rango de fechas al pie para mayor claridad
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
