package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvExportScreen(
    categories: List<String>,
    onClose: () -> Unit,
    onExport: (startDate: Long, endDate: Long, negateExpense: Boolean, category: String, sortBy: String, separator: String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var startDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var negateExpense by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("All Categories") }
    var sortBy by remember { mutableStateOf("Date (Newest First)") }
    var separator by remember { mutableStateOf("Comma") }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showSeparatorDialog by remember { mutableStateOf(false) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd MM月 yyyy", Locale.CHINA) }

    val datePickerColors = DatePickerDefaults.colors(
        containerColor = BeigeHeader,
        titleContentColor = DarkBrownText,
        headlineContentColor = DarkBrownText,
        weekdayContentColor = DarkBrownText,
        subheadContentColor = DarkBrownText,
        yearContentColor = DarkBrownText,
        currentYearContentColor = DarkBrownText,
        selectedYearContentColor = Color.White,
        selectedYearContainerColor = DarkBrownText,
        dayContentColor = DarkBrownText,
        selectedDayContentColor = Color.White,
        selectedDayContainerColor = DarkBrownText,
        todayContentColor = DarkBrownText,
        todayDateBorderColor = DarkBrownText,
        navigationContentColor = DarkBrownText
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { }, // Bloquea toques al fondo
        color = BeigeHeader
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    focusManager.clearFocus()
                    onClose() 
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkBrownText)
                }
                Spacer(Modifier.width(8.dp))
                Text("Export to CSV", color = DarkBrownText, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            }

            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { 
                        focusManager.clearFocus()
                        onExport(startDate, endDate, negateExpense, selectedCategory, sortBy, separator) 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.width(180.dp)
                ) {
                    Text("Export Now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                SectionHeaderWithHelp("Date Range")
                CsvTable {
                    CsvOptionRow(
                        label = "Start Date", 
                        value = dateFormatter.format(Date(startDate)), 
                        onClick = { 
                            focusManager.clearFocus()
                            showStartDatePicker = true 
                        }
                    )
                    CsvOptionRow(
                        label = "End Date", 
                        value = dateFormatter.format(Date(endDate)), 
                        onClick = { 
                            focusManager.clearFocus()
                            showEndDatePicker = true 
                        }, 
                        isLast = true
                    )
                }

                Spacer(Modifier.height(24.dp))

                SectionHeaderWithHelp("Options")
                CsvTable {
                    CsvToggleRow(label = "Negate Expense", checked = negateExpense, onCheckedChange = { negateExpense = it })
                    CsvOptionRow(label = "Category", value = selectedCategory, onClick = { 
                        focusManager.clearFocus()
                        showCategoryDialog = true 
                    })
                    CsvOptionRow(label = "Sort By", value = sortBy, onClick = { 
                        focusManager.clearFocus()
                        showSortDialog = true 
                    })
                    CsvOptionRow(label = "Separator", value = separator, onClick = { 
                        focusManager.clearFocus()
                        showSeparatorDialog = true 
                    }, isLast = true)
                }
            }
        }
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDate = it }
                    showStartDatePicker = false
                }) { Text("OK", color = DarkBrownText) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("CANCEL", color = DarkBrownText) }
            },
            shape = RoundedCornerShape(16.dp)
        ) {
            Surface(color = BeigeHeader, shape = RoundedCornerShape(16.dp)) {
                DatePicker(
                    state = datePickerState, 
                    colors = datePickerColors,
                    showModeToggle = false,
                    title = {},
                    headline = {}
                )
            }
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { endDate = it }
                    showEndDatePicker = false
                }) { Text("OK", color = DarkBrownText) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("CANCEL", color = DarkBrownText) }
            },
            shape = RoundedCornerShape(16.dp)
        ) {
            Surface(color = BeigeHeader, shape = RoundedCornerShape(16.dp)) {
                DatePicker(
                    state = datePickerState, 
                    colors = datePickerColors,
                    showModeToggle = false,
                    title = {},
                    headline = {}
                )
            }
        }
    }

    if (showCategoryDialog) {
        SelectionDialog(
            title = "Category",
            options = listOf("All Categories") + categories,
            selectedOption = selectedCategory,
            onOptionSelected = { selectedCategory = it; showCategoryDialog = false },
            onDismiss = { showCategoryDialog = false }
        )
    }

    if (showSortDialog) {
        SelectionDialog(
            title = "Sort By",
            options = listOf("Date (Newest First)", "Date (Oldest First)", "Amount (Highest First)", "Amount (Lowest First)"),
            selectedOption = sortBy,
            onOptionSelected = { sortBy = it; showSortDialog = false },
            onDismiss = { showSortDialog = false }
        )
    }

    if (showSeparatorDialog) {
        SelectionDialog(
            title = "Separator",
            options = listOf("Comma", "Semicolon", "Tab"),
            selectedOption = separator,
            onOptionSelected = { separator = it; showSeparatorDialog = false },
            onDismiss = { showSeparatorDialog = false }
        )
    }
}

@Composable
fun CsvTable(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 1.dp).border(0.5.dp, Color.LightGray),
        color = Color.White 
    ) {
        Column(content = content)
    }
}

@Composable
fun SectionHeaderWithHelp(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0)) 
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.Gray, fontSize = 15.sp)
        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun CsvOptionRow(label: String, value: String, onClick: () -> Unit, isLast: Boolean = false) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color(0xFF1976D2), modifier = Modifier.width(110.dp), fontSize = 15.sp)
            Text(value, color = Color.Black, fontSize = 17.sp)
        }
        if (!isLast) HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp, color = Color.LightGray)
    }
}

@Composable
fun CsvToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color(0xFF1976D2), modifier = Modifier.width(110.dp), fontSize = 15.sp)
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF4E342E),
                    checkedTrackColor = Color(0xFF4E342E).copy(alpha = 0.4f),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp, color = Color.LightGray)
    }
}

@Composable
fun SelectionDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontSize = 20.sp, fontWeight = FontWeight.Normal) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == selectedOption),
                            onClick = { onOptionSelected(option) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4E342E))
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(text = option, fontSize = 16.sp, color = Color.Black)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Box(modifier = Modifier.fillMaxWidth().padding(end = 8.dp), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = onDismiss) {
                    Text("CANCEL", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(2.dp)
    )
}
