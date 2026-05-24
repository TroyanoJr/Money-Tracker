package net.micode.spendingtracker.ui.screens

import android.app.Activity
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import net.micode.spendingtracker.model.Category
import net.micode.spendingtracker.ui.components.CategoryRow
import net.micode.spendingtracker.ui.components.CategoryTabButton
import net.micode.spendingtracker.ui.components.ChalkDatePickerDialog
import net.micode.spendingtracker.ui.components.SectionHeader
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.util.InterstitialAdHelper
import net.micode.spendingtracker.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility to evaluate simple math expressions (addition/subtraction).
 */
fun evaluateMathExpression(input: String): Double? {
    if (input.isBlank()) return null
    try {
        val cleanInput = input.replace(",", ".").replace(" ", "")
        cleanInput.toDoubleOrNull()?.let { return it }
        val tokens = mutableListOf<String>()
        var currentNumber = ""
        for (char in cleanInput) {
            if (char == '+' || char == '-') {
                if (currentNumber.isNotEmpty()) tokens.add(currentNumber)
                tokens.add(char.toString()); currentNumber = ""
            } else { currentNumber += char }
        }
        if (currentNumber.isNotEmpty()) tokens.add(currentNumber)
        if (tokens.isEmpty() || tokens.last() == "+" || tokens.last() == "-") return null
        var result = tokens[0].toDoubleOrNull() ?: return null
        var i = 1
        while (i < tokens.size) {
            val op = tokens[i]
            val nextVal = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: return null
            result = if (op == "+") result + nextVal else result - nextVal
            i += 2
        }
        return result
    } catch (e: Exception) { return null }
}

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
    val context = LocalContext.current

    // Pre-load interstitial ad when screen opens
    LaunchedEffect(Unit) {
        InterstitialAdHelper.loadAd(context)
    }

    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val currentDashboardDate by viewModel.selectedDate.collectAsState()

    var amount by rememberSaveable { mutableStateOf(transactionToEdit?.amount?.toString() ?: "") }
    var note by rememberSaveable { mutableStateOf(transactionToEdit?.note ?: "") }
    var isRepeating by rememberSaveable { mutableStateOf(transactionToEdit?.isRepeating ?: false) }
    var showCategorySelector by rememberSaveable { mutableStateOf(false) }
    var selectedCategoryName by rememberSaveable { mutableStateOf(transactionToEdit?.categoryName ?: "") }

    val calculatedAmount by remember(amount) { derivedStateOf { evaluateMathExpression(amount) } }
    val isAmountValid by remember(calculatedAmount) { derivedStateOf { calculatedAmount != null } }
    val isCategorySelected by remember(selectedCategoryName) { derivedStateOf { selectedCategoryName.isNotEmpty() } }
    val isFormValid by remember(isAmountValid, isCategorySelected) { derivedStateOf { isAmountValid && isCategorySelected } }

    var selectedLocalDateMillis by rememberSaveable { mutableLongStateOf(transactionToEdit?.date ?: currentDashboardDate) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val formattedDate = dateFormatter.format(Date(selectedLocalDateMillis))

    if (showCategorySelector) {
        val currentCategories = if (pagerState.currentPage == 0) expenseCategories else incomeCategories
        CategorySelectorScreen(
            categories = currentCategories,
            onCategorySelected = { category ->
                selectedCategoryName = category.name
                showCategorySelector = false
            },
            onBack = { showCategorySelector = false }
        )
        return 
    }

    Surface(modifier = Modifier.fillMaxSize().pointerInput(Unit) { }, color = BeigeHeader) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { focusManager.clearFocus(); onClose() }) {
                    Icon(Icons.Default.Close, stringResource(R.string.close), tint = DarkBrownText)
                }
                Text(
                    text = stringResource(R.string.done),
                    color = if (isFormValid) DarkBrownText else DarkBrownText.copy(alpha = 0.3f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 16.dp).clickable(enabled = isFormValid) {
                        if (isFormValid) {
                            val transaction = Transaction(
                                id = transactionToEdit?.id ?: UUID.randomUUID().toString(),
                                amount = calculatedAmount ?: 0.0,
                                categoryName = selectedCategoryName,
                                date = selectedLocalDateMillis,
                                note = note,
                                isExpense = pagerState.currentPage == 0,
                                isRepeating = isRepeating
                            )
                            focusManager.clearFocus()
                            if (transactionToEdit == null) viewModel.addTransaction(transaction)
                            else viewModel.updateTransaction(transaction)
                            
                            // Show ad and then execute the onDone callback
                            InterstitialAdHelper.showAd(context as Activity) {
                                onDone()
                            }
                        }
                    }
                )
            }

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center) {
                CategoryTabButton(text = stringResource(R.string.expense), selected = pagerState.currentPage == 0, isStart = true, onClick = { focusManager.clearFocus(); selectedCategoryName = ""; coroutineScope.launch { pagerState.animateScrollToPage(0) } })
                CategoryTabButton(text = stringResource(R.string.income), selected = pagerState.currentPage == 1, isStart = false, onClick = { focusManager.clearFocus(); selectedCategoryName = ""; coroutineScope.launch { pagerState.animateScrollToPage(1) } })
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { _ ->
                Column(modifier = Modifier.fillMaxSize()) {
                    SectionHeader(title = stringResource(R.string.transaction_details))
                    CategoryRow(label = stringResource(R.string.date), labelColor = Color(0xFF1976D2)) {
                        Text(text = formattedDate, color = DarkBrownText, modifier = Modifier.fillMaxSize().clickable { focusManager.clearFocus(); showDatePicker = true }, fontSize = 16.sp)
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    CategoryRow(label = stringResource(R.string.category), labelColor = if (isCategorySelected) Color(0xFF1976D2) else Color.Red.copy(alpha = 0.7f)) {
                        Box(modifier = Modifier.fillMaxSize().clickable { focusManager.clearFocus(); showCategorySelector = true }, contentAlignment = Alignment.CenterStart) {
                            Text(text = if (selectedCategoryName.isNotEmpty()) selectedCategoryName else stringResource(R.string.category_not_selected), color = if (selectedCategoryName.isNotEmpty()) DarkBrownText else Color.Gray, fontSize = 16.sp)
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    CategoryRow(label = stringResource(R.string.amount), labelColor = if (isAmountValid) Color(0xFF1976D2) else Color.Red.copy(alpha = 0.7f)) {
                        BasicTextField(value = amount, onValueChange = { amount = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), textStyle = TextStyle(fontSize = 16.sp, color = DarkBrownText), modifier = Modifier.fillMaxWidth(), decorationBox = { innerTextField ->
                            if (amount.isEmpty()) Text(stringResource(R.string.amount), color = Color.Gray.copy(alpha = 0.5f))
                            innerTextField()
                        })
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader(title = stringResource(R.string.repeating_details))
                    CategoryRow(label = stringResource(R.string.repeat), labelColor = Color(0xFF1976D2)) {
                        Switch(checked = isRepeating, onCheckedChange = { focusManager.clearFocus(); isRepeating = it }, colors = SwitchDefaults.colors(checkedThumbColor = DarkBrownText, checkedTrackColor = DarkBrownText.copy(alpha = 0.4f)))
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(24.dp))
                    CategoryRow(label = stringResource(R.string.note), labelColor = Color(0xFF1976D2)) {
                        BasicTextField(value = note, onValueChange = { note = it }, textStyle = TextStyle(fontSize = 16.sp, color = DarkBrownText), modifier = Modifier.fillMaxWidth(), decorationBox = { innerTextField ->
                            if (note.isEmpty()) Text(stringResource(R.string.no_note_entered), color = Color.Gray.copy(alpha = 0.5f))
                            innerTextField()
                        })
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }

    if (showDatePicker) {
        ChalkDatePickerDialog(initialDateMillis = selectedLocalDateMillis, onDateSelected = { selectedLocalDateMillis = it; showDatePicker = false }, onDismiss = { showDatePicker = false })
    }
}
