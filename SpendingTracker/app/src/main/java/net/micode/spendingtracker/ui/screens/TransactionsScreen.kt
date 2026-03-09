package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.R
import net.micode.spendingtracker.model.Transaction
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen that displays the list of transactions with income/expense totals.
 */
@Composable
fun TransactionsScreen(viewModel: TransactionViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeHeader)
    ) {
        // Totals Header: Income (Green) and Expense (Red)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TotalBox(
                amount = viewModel.totalIncome,
                labelColor = Color(0xFF4CAF50), // Green
                modifier = Modifier.weight(1f)
            )
            TotalBox(
                amount = viewModel.totalExpense,
                labelColor = Color(0xFFE57373), // Red
                modifier = Modifier.weight(1f)
            )
        }

        // Transactions List
        Box(modifier = Modifier.weight(1f)) {
            if (viewModel.transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No transactions yet", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.transactions) { transaction ->
                        TransactionItem(transaction)
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            thickness = 0.5.dp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }

        // Bottom action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                IconButton(onClick = { }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = DarkBrownText)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = DarkBrownText)
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

@Composable
fun TransactionItem(transaction: Transaction) {
    val dateFormatter = remember { SimpleDateFormat("EEEE, dd MM月 yyyy", Locale.CHINA) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
