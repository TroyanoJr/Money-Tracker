package net.micode.moneytracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.micode.moneytracker.ui.theme.BlackboardBlack
import net.micode.moneytracker.ui.theme.ChalkWhite
import java.text.SimpleDateFormat
import java.util.*

/**
 * A custom date picker dialog with a "chalk and blackboard" aesthetic.
 * Supports day, month, and year selection.
 * 
 * @param initialDateMillis The initial date to show in the picker, in milliseconds.
 * @param onDateSelected Callback triggered when a date is confirmed.
 * @param onDismiss Callback to close the dialog without selecting a date.
 */
@Composable
fun ChalkDatePickerDialog(
    initialDateMillis: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = initialDateMillis }) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = initialDateMillis }) }
    
    // 0: Calendar, 1: Month Selection, 2: Year Selection
    var viewMode by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            color = BlackboardBlack,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ChalkWhite.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (viewMode == 0) {
                            val newMonth = currentMonth.clone() as Calendar
                            newMonth.add(Calendar.MONTH, -1)
                            currentMonth = newMonth
                        } else {
                            viewMode = 0
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = ChalkWhite)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { 
                            viewMode = if (viewMode == 0) 1 else 0 
                        }
                    ) {
                        Text(
                            text = SimpleDateFormat("MMMM", Locale.getDefault()).format(currentMonth.time),
                            color = ChalkWhite,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Cursive,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentMonth.get(Calendar.YEAR).toString(),
                            color = ChalkWhite.copy(alpha = 0.6f),
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Cursive,
                            modifier = Modifier.clickable { viewMode = 2 }
                        )
                    }

                    IconButton(onClick = {
                        if (viewMode == 0) {
                            val newMonth = currentMonth.clone() as Calendar
                            newMonth.add(Calendar.MONTH, 1)
                            currentMonth = newMonth
                        }
                    }) {
                        if (viewMode == 0) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = ChalkWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (viewMode) {
                    0 -> CalendarView(currentMonth, selectedDate) { day ->
                        val newSelected = currentMonth.clone() as Calendar
                        newSelected.set(Calendar.DAY_OF_MONTH, day)
                        selectedDate = newSelected
                    }
                    1 -> MonthPicker { monthIndex ->
                        val newMonth = currentMonth.clone() as Calendar
                        newMonth.set(Calendar.MONTH, monthIndex)
                        currentMonth = newMonth
                        viewMode = 0
                    }
                    2 -> YearPicker(currentMonth.get(Calendar.YEAR)) { year ->
                        val newMonth = currentMonth.clone() as Calendar
                        newMonth.set(Calendar.YEAR, year)
                        currentMonth = newMonth
                        viewMode = 0
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = ChalkWhite.copy(alpha = 0.6f), fontFamily = FontFamily.Cursive)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        onDateSelected(selectedDate.timeInMillis)
                        onDismiss()
                    }) {
                        Text("OK", color = ChalkWhite, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Grid view displaying days of the month for selection.
 */
@Composable
fun CalendarView(currentMonth: Calendar, selectedDate: Calendar, onDaySelected: (Int) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            val days = listOf("M", "T", "W", "T", "F", "S", "S")
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = ChalkWhite.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOffset = ((currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7

        var dayCount = 1
        for (row in 0..5) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val isCurrentMonthDay = (row > 0 || col >= firstDayOffset) && dayCount <= daysInMonth
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCurrentMonthDay) {
                            val currentDay = dayCount
                            val isSelected = isSameDay(currentDay, currentMonth, selectedDate)
                            val isToday = isSameDay(currentDay, currentMonth, Calendar.getInstance())

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(if (isSelected) ChalkWhite.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(
                                        width = if (isToday) 1.dp else 0.dp,
                                        color = if (isToday) ChalkWhite.copy(alpha = 0.4f) else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { onDaySelected(currentDay) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentDay.toString(),
                                    color = if (isSelected) ChalkWhite else ChalkWhite.copy(alpha = 0.8f),
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily.Cursive,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            dayCount++
                        }
                    }
                }
            }
            if (dayCount > daysInMonth) break
        }
    }
}

/**
 * Grid view for selecting a month.
 */
@Composable
fun MonthPicker(onMonthSelected: (Int) -> Unit) {
    val months = SimpleDateFormat("MMM", Locale.getDefault()).let { sdf ->
        (0..11).map { 
            val cal = Calendar.getInstance().apply { set(Calendar.MONTH, it) }
            sdf.format(cal.time)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.height(250.dp)
    ) {
        items(months.indices.toList()) { index ->
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onMonthSelected(index) }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = months[index],
                    color = ChalkWhite,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Cursive
                )
            }
        }
    }
}

/**
 * Grid view for selecting a year within a 100-year range around the current year.
 */
@Composable
fun YearPicker(currentYear: Int, onYearSelected: (Int) -> Unit) {
    val years = (currentYear - 50..currentYear + 50).toList()
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.height(250.dp)
    ) {
        items(years) { year ->
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onYearSelected(year) }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = year.toString(),
                    color = if (year == currentYear) ChalkWhite else ChalkWhite.copy(alpha = 0.6f),
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = if (year == currentYear) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * Checks if two calendar instances represent the same day.
 */
private fun isSameDay(day: Int, monthCal: Calendar, targetCal: Calendar): Boolean {
    return monthCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
           monthCal.get(Calendar.MONTH) == targetCal.get(Calendar.MONTH) &&
           day == targetCal.get(Calendar.DAY_OF_MONTH)
}
