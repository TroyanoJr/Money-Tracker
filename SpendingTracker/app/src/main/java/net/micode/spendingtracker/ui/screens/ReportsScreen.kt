package net.micode.spendingtracker.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.R
import net.micode.spendingtracker.viewmodel.CashFlowPoint
import net.micode.spendingtracker.viewmodel.CategoryReportItem
import net.micode.spendingtracker.viewmodel.TransactionViewModel
import net.micode.spendingtracker.viewmodel.Period
import net.micode.spendingtracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ReportsScreen(viewModel: TransactionViewModel) {
    val cashFlowData by viewModel.cashFlowData.collectAsState()
    val categoryData by viewModel.categoryReportData.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    // Internal keys for UI logic
    var reportMode by remember { mutableStateOf("categories") }
    var unitMode by remember { mutableStateOf("cash") }
    var chartType by remember { mutableStateOf("bar") }

    val dateFormatter = remember(selectedPeriod) {
        when (selectedPeriod) {
            Period.DAY -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            Period.WEEK -> SimpleDateFormat("'${Locale.getDefault().getDisplayLanguage()}' w, yyyy", Locale.getDefault())
            Period.MONTH -> SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            Period.YEAR -> SimpleDateFormat("yyyy", Locale.getDefault())
        }
    }
    val dateText = dateFormatter.format(Date(selectedDate))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackboardBlack)
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = dateText, color = Color.Gray, fontSize = 14.sp, fontFamily = FontFamily.Cursive)
                
                if (reportMode == "cash_flow") {
                    CashFlowLegend()
                } else {
                    CategoryLegend(categoryData)
                }

                if (reportMode == "categories") {
                    UnitToggle(unitMode, currencySymbol) { unitMode = it }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 45.dp, bottom = 55.dp, start = 50.dp, end = 20.dp)
        ) {
            if (reportMode == "cash_flow") {
                CashFlowChart(data = cashFlowData, symbol = currencySymbol)
            } else {
                if (chartType == "bar") {
                    BarChart(data = categoryData, symbol = currencySymbol, usePercentage = unitMode == "percentage")
                } else {
                    PieChart(data = categoryData, symbol = currencySymbol, usePercentage = unitMode == "percentage")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                IconButton(onClick = { viewModel.previousPeriod() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = ChalkWhite, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { viewModel.nextPeriod() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, stringResource(R.string.next), tint = ChalkWhite, modifier = Modifier.size(20.dp))
                }
            }

            ModeToggle(reportMode) { reportMode = it }

            if (reportMode == "categories") {
                ChartTypeToggle(chartType) { chartType = it }
            } else {
                Spacer(Modifier.width(80.dp))
            }
        }
    }
}

@Composable
fun CashFlowLegend() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ChalkGreen))
        Text(" " + stringResource(R.string.income) + " ", color = ChalkWhite, fontSize = 12.sp, fontFamily = FontFamily.Cursive)
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.size(8.dp).background(ChalkRed))
        Text(" " + stringResource(R.string.expense), color = ChalkWhite, fontSize = 12.sp, fontFamily = FontFamily.Cursive)
    }
}

@Composable
fun CategoryLegend(data: List<CategoryReportItem>) {
    Row(
        modifier = Modifier
            .widthIn(max = 250.dp)
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        data.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                Box(modifier = Modifier.size(10.dp).background(Color(item.color)))
                Text(" ${item.name}", color = ChalkWhite, fontSize = 11.sp, maxLines = 1)
            }
        }
    }
}

@Composable
fun UnitToggle(currentUnit: String, symbol: String, onUnitChange: (String) -> Unit) {
    Row(modifier = Modifier.border(0.5.dp, Color.Gray, RoundedCornerShape(2.dp))) {
        listOf("%", symbol).forEach { unit ->
            val label = if (unit == "%") "percentage" else "cash"
            Box(
                modifier = Modifier
                    .clickable { onUnitChange(label) }
                    .background(if (currentUnit == label) Color.Gray.copy(alpha = 0.4f) else Color.Transparent)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(unit, color = ChalkWhite, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ModeToggle(currentMode: String, onModeChange: (String) -> Unit) {
    Row(modifier = Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))) {
        listOf("categories", "cash_flow").forEach { mode ->
            Box(
                modifier = Modifier
                    .clickable { onModeChange(mode) }
                    .background(if (currentMode == mode) Color.Gray.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (mode == "categories") stringResource(R.string.rep_categories) else stringResource(R.string.rep_cash_flow), 
                    color = ChalkWhite, 
                    fontSize = 13.sp, 
                    fontFamily = FontFamily.Cursive
                )
            }
        }
    }
}

@Composable
fun ChartTypeToggle(currentType: String, onTypeChange: (String) -> Unit) {
    Row(modifier = Modifier.border(0.5.dp, Color.Gray, RoundedCornerShape(2.dp))) {
        listOf("pie", "bar").forEach { type ->
            Box(
                modifier = Modifier
                    .clickable { onTypeChange(type) }
                    .background(if (currentType == type) Color.Gray.copy(alpha = 0.4f) else Color.Transparent)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (type == "pie") stringResource(R.string.rep_pie) else stringResource(R.string.rep_bar), 
                    color = ChalkWhite, 
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun CashFlowChart(data: List<CashFlowPoint>, symbol: String) {
    if (data.isEmpty()) return
    val maxVal = (data.maxOfOrNull { it.income.coerceAtLeast(it.expense) } ?: 1.0).coerceAtLeast(1.0)
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val chartWidth = size.width
        val chartHeight = size.height
        
        val gridLines = 5
        for (i in 0..gridLines) {
            val y = chartHeight - (i * chartHeight / gridLines)
            drawLine(Color.Gray.copy(alpha = 0.2f), Offset(0f, y), Offset(chartWidth, y), strokeWidth = 1f)
            
            drawContext.canvas.nativeCanvas.drawText(
                "$symbol${String.format("%.0f", i * maxVal / gridLines)}",
                -10f, y + 10f,
                Paint().apply { color = android.graphics.Color.GRAY; textSize = 24f; textAlign = Paint.Align.RIGHT }
            )
        }

        val stepX = chartWidth / (data.size - 1).coerceAtLeast(1)
        val incomePath = androidx.compose.ui.graphics.Path()
        val expensePath = androidx.compose.ui.graphics.Path()
        
        data.forEachIndexed { index, point ->
            val x = index * stepX
            val yInc = chartHeight - (point.income.toFloat() / maxVal.toFloat() * chartHeight)
            val yExp = chartHeight - (point.expense.toFloat() / maxVal.toFloat() * chartHeight)
            
            if (index == 0) {
                incomePath.moveTo(x, yInc)
                expensePath.moveTo(x, yExp)
            } else {
                incomePath.lineTo(x, yInc)
                expensePath.lineTo(x, yExp)
            }
            
            drawCircle(ChalkGreen, 3.dp.toPx(), Offset(x, yInc))
            drawCircle(ChalkRed, 3.dp.toPx(), Offset(x, yExp))
            
            drawContext.canvas.nativeCanvas.drawText(
                point.label,
                x, chartHeight + 35f,
                Paint().apply { color = android.graphics.Color.GRAY; textSize = 22f; textAlign = Paint.Align.CENTER }
            )
        }
        
        drawPath(incomePath, ChalkGreen, style = Stroke(width = 2.dp.toPx()))
        drawPath(expensePath, ChalkRed, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun BarChart(data: List<CategoryReportItem>, symbol: String, usePercentage: Boolean) {
    if (data.isEmpty()) return
    val maxVal = data.maxOfOrNull { it.amount } ?: 1.0
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val chartWidth = size.width
        val chartHeight = size.height
        val totalBars = data.size
        val barWidth = chartWidth / (totalBars * 1.5f).coerceAtLeast(1f)
        val spacing = (chartWidth - (barWidth * totalBars)) / (totalBars + 1).coerceAtLeast(1)

        data.forEachIndexed { index, item ->
            val x = spacing + index * (barWidth + spacing)
            val barH = (item.amount / maxVal * chartHeight).toFloat()
            val y = chartHeight - barH
            
            drawRect(color = Color(item.color), topLeft = Offset(x, y), size = Size(barWidth, barH))
            
            val label = if (usePercentage) "${(item.percentage * 100).toInt()}%" else "$symbol${String.format("%.0f", item.amount)}"
            drawContext.canvas.nativeCanvas.drawText(
                label, x + barWidth / 2, y - 10f,
                Paint().apply { color = android.graphics.Color.WHITE; textSize = 22f; textAlign = Paint.Align.CENTER }
            )
            
            drawContext.canvas.nativeCanvas.drawText(
                item.name, x + barWidth / 2, chartHeight + 30f,
                Paint().apply { color = android.graphics.Color.GRAY; textSize = 18f; textAlign = Paint.Align.CENTER }
            )
        }
    }
}

@Composable
fun PieChart(data: List<CategoryReportItem>, symbol: String, usePercentage: Boolean) {
    if (data.isEmpty()) return
    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = size.minDimension / 2.2f
        val center = Offset(size.width / 2, size.height / 2)
        var startAngle = -90f
        
        data.forEach { item ->
            val sweepAngle = item.percentage * 360f
            drawArc(
                color = Color(item.color),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )
            
            if (sweepAngle > 15f) {
                val angle = startAngle + sweepAngle / 2
                val labelRad = radius * 1.3f
                val x = center.x + labelRad * cos(Math.toRadians(angle.toDouble())).toFloat()
                val y = center.y + labelRad * sin(Math.toRadians(angle.toDouble())).toFloat()
                
                val label = if (usePercentage) "${(item.percentage * 100).toInt()}%" else "$symbol${String.format("%.0f", item.amount)}"
                drawContext.canvas.nativeCanvas.drawText(
                    label, x, y,
                    Paint().apply { color = android.graphics.Color.WHITE; textSize = 22f; textAlign = Paint.Align.CENTER }
                )
            }
            startAngle += sweepAngle
        }
    }
}
