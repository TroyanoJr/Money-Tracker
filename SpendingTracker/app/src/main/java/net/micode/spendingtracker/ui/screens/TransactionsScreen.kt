package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.model.Transaction
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionsScreen(
    viewModel: TransactionViewModel,
    onEditTransaction: (Transaction) -> Unit,
    onDeleteTransactions: (List<Transaction>) -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()

    // Estado para la selección múltiple
    var selectedTransactionIds by remember { mutableStateOf(setOf<String>()) }
    
    // Estado para el diálogo de confirmación de borrado
    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionsToDelete by remember { mutableStateOf<List<Transaction>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeHeader)
    ) {
        // Barra de herramientas dinámica cuando hay selección
        if (selectedTransactionIds.isNotEmpty()) {
            CategorySelectionToolbar(
                selectedCount = selectedTransactionIds.size,
                onClearSelection = { selectedTransactionIds = emptySet() },
                onEdit = {
                    val transaction = transactions.find { it.id == selectedTransactionIds.first() }
                    transaction?.let { onEditTransaction(it) }
                    selectedTransactionIds = emptySet()
                },
                onDelete = {
                    transactionsToDelete = transactions.filter { it.id in selectedTransactionIds }
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
            TotalBox(
                amount = totalIncome,
                labelColor = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            TotalBox(
                amount = totalExpense,
                labelColor = Color(0xFFE57373),
                modifier = Modifier.weight(1f)
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No transactions for this period", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(transactions) { transaction ->
                        val isSelected = selectedTransactionIds.contains(transaction.id)
                        TransactionItem(
                            transaction = transaction,
                            isSelected = isSelected,
                            onClick = {
                                if (selectedTransactionIds.isEmpty()) {
                                    onEditTransaction(transaction)
                                } else {
                                    selectedTransactionIds = if (isSelected) {
                                        selectedTransactionIds - transaction.id
                                    } else {
                                        selectedTransactionIds + transaction.id
                                    }
                                }
                            },
                            onLongClick = {
                                selectedTransactionIds = selectedTransactionIds + transaction.id
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            thickness = 0.5.dp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                IconButton(onClick = { viewModel.previousPeriod() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Period", tint = DarkBrownText)
                }
                IconButton(onClick = { viewModel.nextPeriod() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Period", tint = DarkBrownText)
                }
            }
            Row {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = DarkBrownText)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Build, contentDescription = "Filter", tint = DarkBrownText)
                }
            }
        }
    }

    // Diálogo de confirmación de borrado
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { 
                Text(
                    text = "Delete Transaction", 
                    color = DarkBrownText,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Text(
                    text = "Are you sure you want to delete the selected transactions?",
                    color = DarkBrownText
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTransactions(transactionsToDelete)
                        selectedTransactionIds = emptySet()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = DarkBrownText)
                }
            },
            containerColor = BeigeHeader
        )
    }
}

@Composable
fun TotalBox(amount: Double, labelColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(40.dp),
        color = labelColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(2.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "¥ ${String.format("%.2f", amount)}",
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
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("EEEE, dd MM月 yyyy", Locale.CHINA) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Color(0xFFE0F7FA) else Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = transaction.categoryIcon,
            contentDescription = null,
            tint = DarkBrownText,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = transaction.categoryName, color = DarkBrownText, fontSize = 18.sp)
            Text(
                text = dateFormatter.format(Date(transaction.date)),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        Text(
            text = "¥ ${String.format("%.2f", transaction.amount)}",
            color = if (transaction.isExpense) Color(0xFFD32F2F) else Color(0xFF388E3C),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
