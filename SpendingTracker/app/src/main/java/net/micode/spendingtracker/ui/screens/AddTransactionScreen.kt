package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.micode.spendingtracker.R
import net.micode.spendingtracker.model.Transaction
import net.micode.spendingtracker.ui.components.CategoryRow
import net.micode.spendingtracker.ui.components.CategoryTabButton
import net.micode.spendingtracker.ui.components.SectionHeader
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionToEdit: Transaction? = null,
    viewModel: TransactionViewModel,
    initialType: Int = 0, // 0 for Expense, 1 for Income
    onClose: () -> Unit,
    onDone: () -> Unit
) {
    val initialPage = transactionToEdit?.let { if (it.isExpense) 0 else 1 } ?: initialType
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    
    val currentDashboardDate by viewModel.selectedDate.collectAsState()

    var amount by remember { mutableStateOf(transactionToEdit?.amount?.toString() ?: "") }
    var note by remember { mutableStateOf(transactionToEdit?.note ?: "") }
    var isRepeating by remember { mutableStateOf(transactionToEdit?.isRepeating ?: false) }
    
    var selectedCategoryIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(expenseCategories, incomeCategories, transactionToEdit) {
        if (transactionToEdit != null) {
            val categories = if (transactionToEdit.isExpense) expenseCategories else incomeCategories
            selectedCategoryIndex = categories.indexOfFirst { it.name == transactionToEdit.categoryName }
        }
    }

    var showCategoryMenu by remember { mutableStateOf(false) }

    val initialDate = transactionToEdit?.date ?: currentDashboardDate
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate)
    val dateFormatter = remember { SimpleDateFormat("dd MM月 yyyy", Locale.CHINA) }
    val formattedDate = dateFormatter.format(Date(datePickerState.selectedDateMillis ?: initialDate))

    var lastPage by remember { mutableIntStateOf(initialPage) }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != lastPage) {
            selectedCategoryIndex = -1
            lastPage = pagerState.currentPage
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { }, // Bloquea toques al fondo
        color = BeigeHeader
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    focusManager.clearFocus()
                    onClose()
                }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = DarkBrownText)
                }
                Text(
                    text = stringResource(R.string.done),
                    color = DarkBrownText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 16.dp).clickable {
                        val isExpense = pagerState.currentPage == 0
                        val categories = if (isExpense) expenseCategories else incomeCategories
                        
                        if (amount.isNotEmpty() && selectedCategoryIndex != -1 && selectedCategoryIndex < categories.size) {
                            val category = categories[selectedCategoryIndex]
                            val selectedTimestamp = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                            
                            val transaction = Transaction(
                                id = transactionToEdit?.id ?: UUID.randomUUID().toString(),
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                categoryName = category.name,
                                categoryIcon = Icons.Default.Sell,
                                date = selectedTimestamp,
                                note = note,
                                isExpense = isExpense,
                                isRepeating = isRepeating
                            )
                            
                            focusManager.clearFocus()
                            if (transactionToEdit == null) {
                                viewModel.addTransaction(transaction)
                            } else {
                                viewModel.updateTransaction(transaction) 
                            }

                            onDone()
                        }
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CategoryTabButton(
                    text = stringResource(R.string.expense),
                    selected = pagerState.currentPage == 0,
                    isStart = true,
                    onClick = { 
                        focusManager.clearFocus()
                        coroutineScope.launch { pagerState.animateScrollToPage(0) } 
                    }
                )
                CategoryTabButton(
                    text = stringResource(R.string.income),
                    selected = pagerState.currentPage == 1,
                    isStart = false,
                    onClick = { 
                        focusManager.clearFocus()
                        coroutineScope.launch { pagerState.animateScrollToPage(1) } 
                    }
                )
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                val isExpense = page == 0
                val categories = if (isExpense) expenseCategories else incomeCategories

                Column(modifier = Modifier.fillMaxSize()) {
                    SectionHeader(title = stringResource(R.string.transaction_details))

                    CategoryRow(label = stringResource(R.string.date), labelColor = Color(0xFF1976D2)) {
                        Text(
                            text = formattedDate,
                            color = DarkBrownText,
                            modifier = Modifier.fillMaxSize().clickable { 
                                focusManager.clearFocus()
                                showDatePicker = true 
                            },
                            fontSize = 16.sp
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                    CategoryRow(label = stringResource(R.string.category), labelColor = Color(0xFF1976D2)) {
                        Box(modifier = Modifier.fillMaxSize().clickable { 
                            focusManager.clearFocus()
                            showCategoryMenu = true 
                        }, contentAlignment = Alignment.CenterStart) {
                            val categoryText = if (selectedCategoryIndex != -1 && selectedCategoryIndex < categories.size) categories[selectedCategoryIndex].name else stringResource(R.string.not_selected)
                            Text(text = categoryText, color = if (selectedCategoryIndex != -1) DarkBrownText else Color.Gray, fontSize = 16.sp)
                            
                            DropdownMenu(
                                expanded = showCategoryMenu && pagerState.currentPage == page, 
                                onDismissRequest = { showCategoryMenu = false },
                                modifier = Modifier.background(BeigeHeader)
                            ) {
                                categories.forEachIndexed { index, category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name, color = DarkBrownText) },
                                        leadingIcon = { Icon(Icons.Default.Sell, contentDescription = null, modifier = Modifier.size(20.dp), tint = DarkBrownText) },
                                        onClick = {
                                            selectedCategoryIndex = index
                                            showCategoryMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                    CategoryRow(label = stringResource(R.string.amount), labelColor = Color(0xFF1976D2)) {
                        BasicTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = TextStyle(fontSize = 16.sp, color = DarkBrownText),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (amount.isEmpty()) Text(stringResource(R.string.amount), color = Color.Gray.copy(alpha = 0.5f))
                                innerTextField()
                            }
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader(title = stringResource(R.string.repeating_details))

                    CategoryRow(label = stringResource(R.string.repeat), labelColor = Color(0xFF1976D2)) {
                        Switch(
                            checked = isRepeating, 
                            onCheckedChange = { 
                                focusManager.clearFocus()
                                isRepeating = it 
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkBrownText,
                                checkedTrackColor = DarkBrownText.copy(alpha = 0.4f)
                            )
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                    Spacer(modifier = Modifier.height(24.dp))

                    CategoryRow(label = stringResource(R.string.note), labelColor = Color(0xFF1976D2)) {
                        BasicTextField(
                            value = note,
                            onValueChange = { note = it },
                            textStyle = TextStyle(fontSize = 16.sp, color = DarkBrownText),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (note.isEmpty()) Text(stringResource(R.string.no_note_entered), color = Color.Gray.copy(alpha = 0.5f))
                                innerTextField()
                            }
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }

    if (showDatePicker) {
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

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { 
                TextButton(onClick = { showDatePicker = false }) { 
                    Text(stringResource(R.string.ok), color = DarkBrownText) 
                } 
            },
            dismissButton = { 
                TextButton(onClick = { showDatePicker = false }) { 
                    Text(stringResource(R.string.cancel), color = DarkBrownText) 
                } 
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
}
