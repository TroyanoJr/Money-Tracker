package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.ui.components.ChalkButton
import net.micode.spendingtracker.ui.components.DottedDivider
import net.micode.spendingtracker.ui.theme.*
import net.micode.spendingtracker.viewmodel.TransactionViewModel

/**
 * Screen that shows the financial overview (Blackboard style).
 * Shows Total Income, Total Expenses with category breakdown, and the final Balance.
 */
@Composable
fun SpendingScreen(
    viewModel: TransactionViewModel,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit
) {
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val expensesByCategory by viewModel.expensesByCategory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackboardBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Period selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = ChalkWhite)
                }
                Text(
                    text = "03月 2026",
                    color = ChalkWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = ChalkWhite)
                }
            }

            // Visual balance bar
            val total = totalIncome + totalExpense
            val incomeWeight = if (total > 0) (totalIncome / total).toFloat() else 0.5f
            val expenseWeight = if (total > 0) (totalExpense / total).toFloat() else 0.5f

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                if (total > 0) {
                    Box(modifier = Modifier.weight(incomeWeight.coerceAtLeast(0.01f)).fillMaxHeight().background(ChalkGreen))
                    Box(modifier = Modifier.weight(expenseWeight.coerceAtLeast(0.01f)).fillMaxHeight().background(ChalkRed))
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.3f)))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Scrollable list for the blackboard content
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Income Row (Total only)
                item {
                    BalanceRow("Income", "¥ ${String.format("%.2f", totalIncome)}", ChalkGreen)
                }

                // Expense Row (Total)
                item {
                    BalanceRow("Expense", "¥ ${String.format("%.2f", totalExpense)}", ChalkRed)
                }

                // Breakdown by Category (ONLY for Expenses)
                items(expensesByCategory) { (name, amount) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, top = 2.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name, color = ChalkWhite, fontSize = 20.sp, fontFamily = FontFamily.Cursive)
                        Text("¥ ${String.format("%.2f", amount)}", color = ChalkWhite, fontSize = 20.sp, fontFamily = FontFamily.Cursive)
                    }
                }

                // Divider and Balance
                item {
                    DottedDivider(modifier = Modifier.padding(vertical = 16.dp))
                    BalanceRow("Balance", "¥ ${String.format("%.2f", balance)}", ChalkBlue)
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChalkButton("+ Expense", onClick = onAddExpense)
                ChalkButton("+ Income", onClick = onAddIncome)
            }
            
            Text(
                "** Rotate device to view reports **",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun BalanceRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = ChalkWhite, fontSize = 28.sp, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 28.sp, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
    }
}
