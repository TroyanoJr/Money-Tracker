package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText

/**
 * Screen for adding a new transaction.
 * Features type selection (Expense/Income), details, and repetition settings.
 * Designed to match the provided reference image.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onClose: () -> Unit,
    onDone: () -> Unit
) {
    // Local state to track user inputs
    var selectedType by remember { mutableIntStateOf(0) } // 0 for Expense, 1 for Income
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isRepeating by remember { mutableStateOf(false) }

    // State for Date Selection
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    
    // Formatter for the date display (e.g., "05 3月 2026")
    val dateFormatter = remember { SimpleDateFormat("dd M月 yyyy", Locale.CHINA) }
    val formattedDate = dateFormatter.format(Date(datePickerState.selectedDateMillis ?: System.currentTimeMillis()))
    
    // UI color constant for labels as seen in the reference
    val labelBlue = Color(0xFF1976D2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeHeader)
    ) {
        // Top Toolbar: contains the close button and the confirmation "Done" text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = DarkBrownText)
            }
            Text(
                text = "Done",
                color = DarkBrownText,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickable { onDone() }
            )
        }

        // Expense / Income Selector: reusable tab style buttons for switching transaction type
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            CategoryTabButton(
                text = "EXPENSE",
                selected = selectedType == 0,
                isStart = true,
                onClick = { selectedType = 0 }
            )
            CategoryTabButton(
                text = "INCOME",
                selected = selectedType == 1,
                isStart = false,
                onClick = { selectedType = 1 }
            )
        }

        // Section: Transaction Details (Date, Category, Amount)
        SectionHeader(title = "Transaction Details")

        // Date Row: displays the selected date and opens the DatePicker on click
        CategoryRow(label = "Date", labelColor = labelBlue) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showDatePicker = true },
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = formattedDate, color = DarkBrownText, fontSize = 16.sp)
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

        // Category Row: leads to category selection
        CategoryRow(label = "Category", labelColor = labelBlue) {
            Text(text = "Not Selected", color = Color.LightGray, fontSize = 16.sp)
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

        // Amount Row: text input for the transaction value
        CategoryRow(label = "Amount", labelColor = labelBlue) {
            BasicTextField(
                value = amount,
                onValueChange = { amount = it },
                textStyle = TextStyle(fontSize = 16.sp, color = DarkBrownText),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    // Show placeholder when empty
                    if (amount.isEmpty()) {
                        Text("Amount", color = Color.LightGray, fontSize = 16.sp)
                    }
                    innerTextField()
                }
            )
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Repeating Details (Toggle for recurring transactions)
        SectionHeader(title = "Repeating Details")

        // Repeat Row: Switch component for toggling repetition
        CategoryRow(label = "Repeat", labelColor = labelBlue) {
            Switch(
                checked = isRepeating,
                onCheckedChange = { isRepeating = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = DarkBrownText,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

        Spacer(modifier = Modifier.height(24.dp))

        // Note Row: optional text field for additional transaction info
        CategoryRow(label = "Note", labelColor = labelBlue) {
            BasicTextField(
                value = note,
                onValueChange = { note = it },
                textStyle = TextStyle(fontSize = 16.sp, color = DarkBrownText),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    // Show placeholder when empty
                    if (note.isEmpty()) {
                        Text("No Note Entered", color = Color.LightGray, fontSize = 16.sp)
                    }
                    innerTextField()
                }
            )
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
    }

    // Material 3 DatePickerDialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("确定", color = DarkBrownText) // "OK" in Chinese
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消", color = DarkBrownText) // "Cancel" in Chinese
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color.White
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    headlineContentColor = DarkBrownText,
                    titleContentColor = DarkBrownText,
                    todayContentColor = DarkBrownText,
                    todayDateBorderColor = DarkBrownText,
                    selectedDayContainerColor = DarkBrownText,
                    selectedDayContentColor = Color.White
                )
            )
        }
    }
}

/**
 * Shared section header component with a background color and help icon.
 */
@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Icon(
            Icons.Default.HelpOutline,
            contentDescription = "Help",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}
