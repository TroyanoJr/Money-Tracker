package net.micode.moneytracker.ui.screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.moneytracker.ui.components.ChalkDatePickerDialog
import net.micode.moneytracker.ui.theme.BeigeHeader
import net.micode.moneytracker.ui.theme.DarkBrownText
import java.text.SimpleDateFormat
import java.util.*
import net.micode.moneytracker.R
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
    
    // Internal keys for logic
    var selectedCategory by remember { mutableStateOf("all") }
    var sortBy by remember { mutableStateOf("date_new") }
    var separator by remember { mutableStateOf("comma") }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showSeparatorDialog by remember { mutableStateOf(false) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Surface(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) { }, 
        color = BeigeHeader
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { focusManager.clearFocus(); onClose() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = DarkBrownText)
                }
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.export_to_csv), color = DarkBrownText, fontSize = 20.sp, fontWeight = FontWeight.Medium)
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
                    Text(stringResource(R.string.export_now), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                SectionHeaderWithHelp(stringResource(R.string.date_range))
                CsvTable {
                    CsvOptionRow(
                        label = stringResource(R.string.start_date), 
                        value = dateFormatter.format(Date(startDate)), 
                        onClick = { focusManager.clearFocus(); showStartDatePicker = true }
                    )
                    CsvOptionRow(
                        label = stringResource(R.string.end_date), 
                        value = dateFormatter.format(Date(endDate)), 
                        onClick = { focusManager.clearFocus(); showEndDatePicker = true }, 
                        isLast = true
                    )
                }

                Spacer(Modifier.height(24.dp))

                SectionHeaderWithHelp(stringResource(R.string.options))
                CsvTable {
                    CsvToggleRow(label = stringResource(R.string.negate_expense), checked = negateExpense, onCheckedChange = { negateExpense = it })
                    CsvOptionRow(
                        label = stringResource(R.string.category), 
                        value = if (selectedCategory == "all") stringResource(R.string.all_categories) else selectedCategory, 
                        onClick = { focusManager.clearFocus(); showCategoryDialog = true }
                    )
                    CsvOptionRow(
                        label = stringResource(R.string.sort_by), 
                        value = when(sortBy) {
                            "date_new" -> stringResource(R.string.sort_date_newest)
                            "date_old" -> stringResource(R.string.sort_date_oldest)
                            "amount_high" -> stringResource(R.string.sort_amount_highest)
                            else -> stringResource(R.string.sort_amount_lowest)
                        }, 
                        onClick = { focusManager.clearFocus(); showSortDialog = true }
                    )
                    CsvOptionRow(
                        label = stringResource(R.string.separator), 
                        value = when(separator) {
                            "comma" -> stringResource(R.string.sep_comma)
                            "semicolon" -> stringResource(R.string.sep_semicolon)
                            else -> stringResource(R.string.sep_tab)
                        }, 
                        onClick = { focusManager.clearFocus(); showSeparatorDialog = true }, 
                        isLast = true
                    )
                }
            }
        }
    }

    if (showStartDatePicker) {
        ChalkDatePickerDialog(initialDateMillis = startDate, onDateSelected = { startDate = it; showStartDatePicker = false }, onDismiss = { showStartDatePicker = false })
    }

    if (showEndDatePicker) {
        ChalkDatePickerDialog(initialDateMillis = endDate, onDateSelected = { endDate = it; showEndDatePicker = false }, onDismiss = { showEndDatePicker = false })
    }

    if (showCategoryDialog) {
        val catOptions = listOf("all") + categories
        val catLabels = listOf(stringResource(R.string.all_categories)) + categories
        SelectionDialog(
            title = stringResource(R.string.category),
            options = catOptions,
            displayLabels = catLabels,
            selectedOption = selectedCategory,
            onOptionSelected = { selectedCategory = it; showCategoryDialog = false },
            onDismiss = { showCategoryDialog = false }
        )
    }

    if (showSortDialog) {
        val sortOptions = listOf("date_new", "date_old", "amount_high", "amount_low")
        val sortLabels = listOf(stringResource(R.string.sort_date_newest), stringResource(R.string.sort_date_oldest), stringResource(R.string.sort_amount_highest), stringResource(R.string.sort_amount_lowest))
        SelectionDialog(
            title = stringResource(R.string.sort_by),
            options = sortOptions,
            displayLabels = sortLabels,
            selectedOption = sortBy,
            onOptionSelected = { sortBy = it; showSortDialog = false },
            onDismiss = { showSortDialog = false }
        )
    }

    if (showSeparatorDialog) {
        val sepOptions = listOf("comma", "semicolon", "tab")
        val sepLabels = listOf(stringResource(R.string.sep_comma), stringResource(R.string.sep_semicolon), stringResource(R.string.sep_tab))
        SelectionDialog(
            title = stringResource(R.string.separator),
            options = sepOptions,
            displayLabels = sepLabels,
            selectedOption = separator,
            onOptionSelected = { separator = it; showSeparatorDialog = false },
            onDismiss = { showSeparatorDialog = false }
        )
    }
}

@Composable
fun CsvTable(content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 1.dp).border(0.5.dp, Color.LightGray), color = Color.White) { Column(content = content) }
}

@Composable
fun SectionHeaderWithHelp(title: String) {
    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFE0E0E0)).padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = Color.Gray, fontSize = 15.sp)
        Icon(Icons.AutoMirrored.Filled.HelpOutline, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun CsvOptionRow(label: String, value: String, onClick: () -> Unit, isLast: Boolean = false) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color(0xFF1976D2), modifier = Modifier.width(110.dp), fontSize = 15.sp)
            Text(value, color = Color.Black, fontSize = 17.sp)
        }
        if (!isLast) HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp, color = Color.LightGray)
    }
}

@Composable
fun CsvToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color(0xFF1976D2), modifier = Modifier.width(110.dp), fontSize = 15.sp)
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF4E342E), checkedTrackColor = Color(0xFF4E342E).copy(alpha = 0.4f), uncheckedThumbColor = Color.Gray, uncheckedTrackColor = Color.LightGray))
        }
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp, color = Color.LightGray)
    }
}

@Composable
fun SelectionDialog(
    title: String,
    options: List<String>,
    displayLabels: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontSize = 20.sp, fontWeight = FontWeight.Normal) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, option ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { onOptionSelected(option) }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = (option == selectedOption), onClick = { onOptionSelected(option) }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4E342E)))
                        Spacer(Modifier.width(16.dp))
                        Text(text = displayLabels[index], fontSize = 16.sp, color = Color.Black)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { Box(modifier = Modifier.fillMaxWidth().padding(end = 8.dp), contentAlignment = Alignment.CenterEnd) { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = Color.Gray, fontWeight = FontWeight.Medium) } } },
        containerColor = Color.White,
        shape = RoundedCornerShape(2.dp)
    )
}
