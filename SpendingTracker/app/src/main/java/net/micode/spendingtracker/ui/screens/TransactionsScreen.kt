package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.collectLatest
import net.micode.spendingtracker.R
import net.micode.spendingtracker.model.Account
import net.micode.spendingtracker.model.Transaction
import net.micode.spendingtracker.ui.components.CategorySelectionToolbar
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.util.IconCatalog
import net.micode.spendingtracker.util.PdfExporter
import net.micode.spendingtracker.util.Period
import net.micode.spendingtracker.viewmodel.FilterType
import net.micode.spendingtracker.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen that displays a paginated list of transactions.
 * Provides filtering by category, account switching, and export options (CSV/PDF).
 * Supports multi-selection for bulk deletion.
 * 
 * Logic Update: Account info is hidden if only one account exists.
 * 
 * @param viewModel ViewModel for transactions.
 * @param accounts List of available accounts.
 * @param onEditTransaction Callback to edit a transaction.
 * @param onDeleteTransactions Callback to delete selected transactions.
 * @param onExportCsv Navigation callback for CSV export.
 * @param onSwitchAccountClick UI callback to switch accounts.
 */
@Composable
fun TransactionsScreen(
    viewModel: TransactionViewModel,
    accounts: List<Account>,
    onEditTransaction: (Transaction) -> Unit,
    onDeleteTransactions: (List<Transaction>) -> Unit,
    onExportCsv: () -> Unit,
    onSwitchAccountClick: () -> Unit
) {
    val context = LocalContext.current
    val pagedTransactions = viewModel.pagedTransactions.collectAsLazyPagingItems()
    
    LaunchedEffect(Unit) {
        viewModel.dataChangedEvent.collectLatest {
            pagedTransactions.refresh()
        }
    }

    val categories by viewModel.categories.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val carryOverAmount by viewModel.carryOverAmount.collectAsState()
    val isCarryOverEnabled by viewModel.isCarryOverEnabled.collectAsState()
    
    val activeFilter by viewModel.activeFilterType.collectAsState()
    val filterCategoryName by viewModel.filterCategoryName.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedAccountId by viewModel.selectedAccountId.collectAsState()

    val currentAccount = remember(selectedAccountId, accounts) {
        accounts.find { it.id == selectedAccountId }
    }

    val currentAccountName = remember(selectedAccountId, accounts) {
        if (selectedAccountId == -1L) "All Accounts"
        else currentAccount?.name ?: "Default"
    }

    // Only show account switching elements if there's more than 1 account
    val isMultiAccount = remember(accounts) { accounts.size > 1 }

    var selectedTransactionIds by remember { mutableStateOf(setOf<String>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionsToDelete by remember { mutableStateOf<List<Transaction>>(emptyList()) }

    var showExportDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeHeader)
    ) {
        if (selectedTransactionIds.isNotEmpty()) {
            CategorySelectionToolbar(
                selectedCount = selectedTransactionIds.size,
                onClearSelection = { selectedTransactionIds = emptySet() },
                onEdit = {
                    val transaction = pagedTransactions.itemSnapshotList.items
                        .find { it.id == selectedTransactionIds.first() }
                    transaction?.let { onEditTransaction(it) }
                    selectedTransactionIds = emptySet()
                },
                onDelete = {
                    transactionsToDelete = pagedTransactions.itemSnapshotList.items
                        .filter { it.id in selectedTransactionIds }
                    showDeleteDialog = true
                }
            )
        }

        /**
         * ACCOUNT NAME HEADER
         * Hidden if only one account exists.
         */
        if (isMultiAccount) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentAccountName,
                    color = DarkBrownText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TotalBox(amount = totalIncome, symbol = currencySymbol, labelColor = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
            TotalBox(amount = totalExpense, symbol = currencySymbol, labelColor = Color(0xFFE57373), modifier = Modifier.weight(1f))
        }

        Box(modifier = Modifier.weight(1f)) {
            val isRefreshing = pagedTransactions.loadState.refresh is LoadState.Loading
            val refreshError = pagedTransactions.loadState.refresh as? LoadState.Error
            
            /**
             * Carry Over Visibility Logic:
             * Carry over should be visible if enabled in settings and no specific filters are applied.
             */
            val shouldShowCarryOver = isCarryOverEnabled && activeFilter == FilterType.ALL
            
            /**
             * Updated Empty State Logic:
             * The screen is only considered empty if there are no transactions AND no carry over to display.
             * This ensures the LazyColumn is rendered even if the only item is the carry over.
             */
            val isEmpty = !isRefreshing && refreshError == null && pagedTransactions.itemCount == 0 && !shouldShowCarryOver

            if (isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DarkBrownText)
                }
            } else if (refreshError != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = stringResource(R.string.load_error), color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { pagedTransactions.retry() }) {
                            Text(stringResource(R.string.retry), color = DarkBrownText)
                        }
                    }
                }
            } else if (isEmpty) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.no_transactions), color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        count = pagedTransactions.itemCount,
                        key = { index -> pagedTransactions[index]?.id ?: "placeholder-$index" }
                    ) { index ->
                        val transaction = pagedTransactions[index] ?: return@items
                        val isSelected = selectedTransactionIds.contains(transaction.id)

                        val category = categories.find { it.name == transaction.categoryName }
                        val icon = if (transaction.categoryName == "Transfer") Icons.Default.SyncAlt else if (category != null) IconCatalog.getIconByName(category.iconName) else Icons.Default.Sell

                        TransactionItem(
                            transaction = transaction,
                            icon = icon,
                            symbol = currencySymbol,
                            isSelected = isSelected,
                            onClick = {
                                if (selectedTransactionIds.isEmpty()) {
                                    onEditTransaction(transaction)
                                } else {
                                    selectedTransactionIds = if (isSelected) selectedTransactionIds - transaction.id else selectedTransactionIds + transaction.id
                                }
                            },
                            onLongClick = { selectedTransactionIds = selectedTransactionIds + transaction.id }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp, color = Color.LightGray)
                    }

                    if (shouldShowCarryOver) {
                        item {
                            CarryOverItem(
                                amount = carryOverAmount,
                                symbol = currencySymbol,
                                date = selectedDate
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp, color = Color.LightGray)
                        }
                    }

                    if (pagedTransactions.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = DarkBrownText)
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                IconButton(onClick = { viewModel.previousPeriod() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = DarkBrownText)
                }
                IconButton(onClick = { viewModel.nextPeriod() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.next), tint = DarkBrownText)
                }
            }
            Row {
                IconButton(onClick = { showExportDialog = true }) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = DarkBrownText)
                }
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = DarkBrownText)
                }
                
                /**
                 * ACCOUNT PICKER BUTTON
                 * Hidden if only one account exists.
                 */
                if (isMultiAccount) {
                    IconButton(onClick = onSwitchAccountClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Switch Account",
                            tint = if (currentAccount != null) Color(currentAccount.color) else DarkBrownText
                        )
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(stringResource(R.string.export_data), fontWeight = FontWeight.Bold, color = DarkBrownText) },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.csv_format), color = DarkBrownText) },
                        modifier = Modifier.clickable {
                            showExportDialog = false
                            onExportCsv()
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.pdf_format), color = DarkBrownText) },
                        modifier = Modifier.clickable {
                            showExportDialog = false
                            val periodLabel = run {
                                val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                                when (selectedPeriod) {
                                    Period.DAY -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(cal.time)
                                    Period.WEEK -> {
                                        val start = cal.clone() as Calendar
                                        start.set(Calendar.DAY_OF_WEEK, start.firstDayOfWeek)
                                        val end = start.clone() as Calendar
                                        end.add(Calendar.DAY_OF_WEEK, 6)
                                        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                                        "${sdf.format(start.time)} - ${sdf.format(end.time)}"
                                    }
                                    Period.MONTH -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
                                    Period.YEAR -> SimpleDateFormat("yyyy", Locale.getDefault()).format(cal.time)
                                }
                            }
                            PdfExporter.exportTransactionsToPdf(
                                context = context,
                                periodName = periodLabel,
                                totalIncome = totalIncome,
                                totalExpense = totalExpense,
                                balance = totalIncome - totalExpense,
                                currencySymbol = currencySymbol,
                                transactions = pagedTransactions.itemSnapshotList.items.filterNotNull()
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showExportDialog = false }) { Text(stringResource(R.string.cancel), color = DarkBrownText) } },
            containerColor = BeigeHeader,
            shape = RoundedCornerShape(4.dp)
        )
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = {
                Text(
                    stringResource(R.string.category_filter),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = DarkBrownText
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                    LazyColumn {
                        item {
                            FilterOptionRow(
                                label = stringResource(R.string.all_categories),
                                selected = activeFilter == FilterType.ALL,
                                onClick = {
                                    viewModel.setFilter(FilterType.ALL)
                                    showFilterDialog = false
                                }
                            )
                        }
                        item {
                            FilterOptionRow(
                                label = stringResource(R.string.all_expense),
                                selected = activeFilter == FilterType.ONLY_EXPENSE,
                                onClick = {
                                    viewModel.setFilter(FilterType.ONLY_EXPENSE)
                                    showFilterDialog = false
                                }
                            )
                        }
                        item {
                            FilterOptionRow(
                                label = stringResource(R.string.all_income),
                                selected = activeFilter == FilterType.ONLY_INCOME,
                                onClick = {
                                    viewModel.setFilter(FilterType.ONLY_INCOME)
                                    showFilterDialog = false
                                }
                            )
                        }
                        items(categories.size) { index ->
                            val category = categories[index]
                            FilterOptionRow(
                                label = category.name,
                                selected = activeFilter == FilterType.BY_CATEGORY && filterCategoryName == category.name,
                                onClick = {
                                    viewModel.setFilter(FilterType.BY_CATEGORY, category.name)
                                    showFilterDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text(stringResource(R.string.cancel), color = DarkBrownText)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.setFilter(FilterType.ALL)
                    showFilterDialog = false
                }) {
                    Text(stringResource(R.string.clear_filter), color = DarkBrownText)
                }
            },
            containerColor = BeigeHeader,
            shape = RoundedCornerShape(4.dp)
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = stringResource(R.string.delete_transaction), color = DarkBrownText, fontWeight = FontWeight.Bold) },
            text = { Text(text = stringResource(R.string.delete_confirm_msg), color = DarkBrownText) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTransactions(transactionsToDelete)
                    selectedTransactionIds = emptySet()
                    showDeleteDialog = false
                }) { Text(stringResource(R.string.delete), color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel), color = DarkBrownText) }
            },
            containerColor = BeigeHeader,
            shape = RoundedCornerShape(4.dp)
        )
    }
}

@Composable
fun FilterOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = DarkBrownText)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = DarkBrownText
        )
    }
}

@Composable
fun TotalBox(amount: Double, symbol: String, labelColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(40.dp),
        color = labelColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(2.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = String.format(Locale.getDefault(), "%s%s %.2f", if (amount >= 0) "+" else "", symbol, amount),
                color = labelColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    icon: ImageVector,
    symbol: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Color(0xFFE0F7FA) else Color.Transparent)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = DarkBrownText, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = transaction.categoryName, color = DarkBrownText, fontSize = 18.sp)
            Text(text = dateFormatter.format(Date(transaction.date)), color = Color.Gray, fontSize = 12.sp)
        }
        Text(
            text = String.format(Locale.getDefault(), "%s%s %.2f", if (transaction.isExpense) "-" else "+", symbol, transaction.amount),
            color = if (transaction.isExpense) Color(0xFFD32F2F) else Color(0xFF388E3C),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CarryOverItem(
    amount: Double,
    symbol: String,
    date: Long
) {
    val dateFormatter = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFB74D).copy(alpha = 0.1f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Savings, contentDescription = null, tint = Color(0xFFE67E22), modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.carry_over), color = DarkBrownText, fontSize = 18.sp)
            Text(text = dateFormatter.format(Date(date)), color = Color.Gray, fontSize = 12.sp)
        }
        Text(
            text = String.format(Locale.getDefault(), "%s%s %.2f", if (amount >= 0) "" else "-", symbol, amount),
            color = Color(0xFFE67E22),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
