package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import net.micode.spendingtracker.model.Transaction
import net.micode.spendingtracker.ui.components.CategorySelectionToolbar
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.viewmodel.FilterType
import net.micode.spendingtracker.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionsScreen(
    viewModel: TransactionViewModel,
    onEditTransaction: (Transaction) -> Unit,
    onDeleteTransactions: (List<Transaction>) -> Unit,
    onExportCsv: () -> Unit
) {
    val pagedTransactions = viewModel.pagedTransactions.collectAsLazyPagingItems()
    val categories by viewModel.categories.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val activeFilter by viewModel.activeFilterType.collectAsState()
    val filterCategoryName by viewModel.filterCategoryName.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

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
            val isEmpty = !isRefreshing && refreshError == null && pagedTransactions.itemCount == 0

            if (isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DarkBrownText)
                }
            } else if (refreshError != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Could not load transactions", color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { pagedTransactions.retry() }) {
                            Text("Retry", color = DarkBrownText)
                        }
                    }
                }
            } else if (isEmpty) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No transactions for this filter/period", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        count = pagedTransactions.itemCount,
                        key = { index -> pagedTransactions[index]?.id ?: "placeholder-$index" }
                    ) { index ->
                        val transaction = pagedTransactions[index] ?: return@items
                        val isSelected = selectedTransactionIds.contains(transaction.id)
                        TransactionItem(
                            transaction = transaction,
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = DarkBrownText)
                }
                IconButton(onClick = { viewModel.nextPeriod() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = DarkBrownText)
                }
            }
            Row {
                IconButton(onClick = { showExportDialog = true }) {
                    Icon(Icons.Default.Share, contentDescription = "Export", tint = DarkBrownText)
                }
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = DarkBrownText)
                }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data", fontWeight = FontWeight.Bold, color = DarkBrownText) },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("CSV Format", color = DarkBrownText) },
                        modifier = Modifier.clickable { 
                            showExportDialog = false
                            onExportCsv()
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    ListItem(
                        headlineContent = { Text("PDF Format", color = DarkBrownText) },
                        modifier = Modifier.clickable { showExportDialog = false },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showExportDialog = false }) { Text("CANCEL", color = DarkBrownText) } },
            containerColor = BeigeHeader,
            shape = RoundedCornerShape(4.dp)
        )
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { 
                Text(
                    "Category Filter", 
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
                                label = "All Categories",
                                selected = activeFilter == FilterType.ALL,
                                onClick = {
                                    viewModel.setFilter(FilterType.ALL)
                                    showFilterDialog = false
                                }
                            )
                        }
                        item {
                            FilterOptionRow(
                                label = "All Expense",
                                selected = activeFilter == FilterType.ONLY_EXPENSE,
                                onClick = {
                                    viewModel.setFilter(FilterType.ONLY_EXPENSE)
                                    showFilterDialog = false
                                }
                            )
                        }
                        item {
                            FilterOptionRow(
                                label = "All Income",
                                selected = activeFilter == FilterType.ONLY_INCOME,
                                onClick = {
                                    viewModel.setFilter(FilterType.ONLY_INCOME)
                                    showFilterDialog = false
                                }
                            )
                        }
                        items(categories) { category ->
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
                    Text("CANCEL", color = DarkBrownText)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.setFilter(FilterType.ALL)
                    showFilterDialog = false
                }) {
                    Text("CLEAR FILTER", color = DarkBrownText)
                }
            },
            containerColor = BeigeHeader,
            shape = RoundedCornerShape(4.dp)
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Delete Transaction", color = DarkBrownText, fontWeight = FontWeight.Bold) },
            text = { Text(text = "Are you sure you want to delete the selected transactions?", color = DarkBrownText) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTransactions(transactionsToDelete)
                    selectedTransactionIds = emptySet()
                    showDeleteDialog = false
                }) { Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel", color = DarkBrownText) }
            },
            containerColor = BeigeHeader
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
                text = String.format(Locale.getDefault(), "%s %.2f", symbol, amount),
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = transaction.categoryIcon, contentDescription = null, tint = DarkBrownText, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = transaction.categoryName, color = DarkBrownText, fontSize = 18.sp)
            Text(text = dateFormatter.format(Date(transaction.date)), color = Color.Gray, fontSize = 12.sp)
        }
        Text(
            text = String.format(Locale.getDefault(), "%s %.2f", symbol, transaction.amount),
            color = if (transaction.isExpense) Color(0xFFD32F2F) else Color(0xFF388E3C),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
