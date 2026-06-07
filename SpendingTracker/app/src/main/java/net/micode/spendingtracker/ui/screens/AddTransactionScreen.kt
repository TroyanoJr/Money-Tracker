package net.micode.spendingtracker.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import net.micode.spendingtracker.model.Account
import net.micode.spendingtracker.model.Transaction
import net.micode.spendingtracker.ui.components.AccountPickerDialog
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
 * Screen for adding a new transaction or editing an existing one.
 * It features a pager to switch between Expense and Income types.
 * 
 * @param transactionToEdit The transaction object to edit, or null if creating a new one.
 * @param viewModel The ViewModel for managing transactions.
 * @param accounts The list of available accounts for transaction assignment.
 * @param initialType The starting transaction type (0 for Expense, 1 for Income).
 * @param onClose Callback to close the screen.
 * @param onDone Callback triggered after a transaction is successfully saved.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionToEdit: Transaction? = null,
    viewModel: TransactionViewModel,
    accounts: List<Account>,
    initialType: Int = 0,
    onClose: () -> Unit,
    onDone: () -> Unit
) {
    val initialPage = transactionToEdit?.let { if (it.isExpense) 0 else 1 } ?: initialType
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // Preload an interstitial ad when the screen is entered
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
    
    var selectedAccountId by rememberSaveable { mutableLongStateOf(transactionToEdit?.accountId ?: 1L) }
    var showAccountPicker by rememberSaveable { mutableStateOf(false) }
    val selectedAccountName = accounts.find { it.id == selectedAccountId }?.name ?: "Default"

    val isAmountValid = amount.isNotEmpty()
    val isCategorySelected = selectedCategoryName.isNotEmpty()
    val isFormValid = isAmountValid && isCategorySelected

    var selectedLocalDateMillis by rememberSaveable { mutableLongStateOf(transactionToEdit?.date ?: currentDashboardDate) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
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

    Surface(modifier = Modifier.fillMaxSize(), color = BeigeHeader) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Close and Done actions
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onClose() }) {
                    Icon(Icons.Default.Close, stringResource(R.string.close), tint = DarkBrownText)
                }
                Text(
                    text = stringResource(R.string.done),
                    color = if (isFormValid) DarkBrownText else DarkBrownText.copy(alpha = 0.3f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 16.dp).clickable(enabled = isFormValid) {
                        val transaction = Transaction(
                            id = transactionToEdit?.id ?: UUID.randomUUID().toString(),
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            categoryName = selectedCategoryName,
                            date = selectedLocalDateMillis,
                            note = note,
                            isExpense = pagerState.currentPage == 0,
                            isRepeating = isRepeating,
                            accountId = selectedAccountId
                        )
                        if (transactionToEdit == null) viewModel.addTransaction(transaction)
                        else viewModel.updateTransaction(transaction)
                        
                        // Show ad before finishing the operation
                        InterstitialAdHelper.showAd(context as Activity) {
                            onDone()
                        }
                    }
                )
            }

            // Tab bar to toggle Expense/Income
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center) {
                CategoryTabButton(text = stringResource(R.string.expense), selected = pagerState.currentPage == 0, isStart = true, onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } })
                CategoryTabButton(text = stringResource(R.string.income), selected = pagerState.currentPage == 1, isStart = false, onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } })
            }

            // Form Content
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { _ ->
                Column(modifier = Modifier.fillMaxSize()) {
                    SectionHeader(title = stringResource(R.string.transaction_details))
                    
                    CategoryRow(label = stringResource(R.string.date), labelColor = MaterialTheme.colorScheme.primary) {
                        Text(text = formattedDate, color = DarkBrownText, modifier = Modifier.fillMaxSize().clickable { showDatePicker = true }, fontSize = 16.sp)
                    }
                    
                    CategoryRow(label = stringResource(R.string.category), labelColor = if (isCategorySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) {
                        Box(modifier = Modifier.fillMaxSize().clickable { showCategorySelector = true }, contentAlignment = Alignment.CenterStart) {
                            Text(text = if (selectedCategoryName.isNotEmpty()) selectedCategoryName else stringResource(R.string.category_not_selected), color = if (selectedCategoryName.isNotEmpty()) DarkBrownText else Color.Gray, fontSize = 16.sp)
                        }
                    }
                    
                    CategoryRow(label = stringResource(R.string.amount), labelColor = if (isAmountValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) {
                        BasicTextField(value = amount, onValueChange = { amount = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), textStyle = TextStyle(fontSize = 16.sp, color = DarkBrownText), modifier = Modifier.fillMaxWidth())
                    }
                    
                    if (accounts.size > 1) {
                        CategoryRow(label = stringResource(R.string.account), labelColor = MaterialTheme.colorScheme.primary) {
                            Box(modifier = Modifier.fillMaxSize().clickable { showAccountPicker = true }, contentAlignment = Alignment.CenterStart) {
                                Text(text = selectedAccountName, color = DarkBrownText, fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader(title = stringResource(R.string.repeating_details))
                    CategoryRow(label = stringResource(R.string.repeat), labelColor = MaterialTheme.colorScheme.primary) {
                        Switch(checked = isRepeating, onCheckedChange = { isRepeating = it }, colors = SwitchDefaults.colors(checkedThumbColor = DarkBrownText, checkedTrackColor = DarkBrownText.copy(alpha = 0.4f)))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    /**
                    CategoryRow(label = stringResource(R.string.note), labelColor = MaterialTheme.colorScheme.primary) {
                        BasicTextField(value = note, onValueChange = { note = it }, textStyle = TextStyle(fontSize = 16.sp, color = DarkBrownText), modifier = Modifier.fillMaxWidth())
                    }
                    **/
                }
            }
        }
    }

    if (showDatePicker) {
        ChalkDatePickerDialog(initialDateMillis = selectedLocalDateMillis, onDateSelected = { selectedLocalDateMillis = it; showDatePicker = false }, onDismiss = { showDatePicker = false })
    }

    if (showAccountPicker) {
        AccountPickerDialog(accounts = accounts, selectedAccountId = selectedAccountId, onAccountSelected = { selectedAccountId = it }, onDismiss = { showAccountPicker = false }, showAllAccountsOption = false)
    }
}
