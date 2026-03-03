package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.ui.components.ChalkButton
import net.micode.spendingtracker.ui.components.DottedDivider
import net.micode.spendingtracker.ui.theme.*

@Composable
fun SpendingScreen() {
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
                Spacer(Modifier.width(20.dp))
                IconButton(onClick = { }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = ChalkWhite)
                }
            }

            // Visual balance bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                Box(modifier = Modifier.weight(0.6f).fillMaxHeight().background(ChalkGreen))
                Box(modifier = Modifier.weight(0.4f).fillMaxHeight().background(ChalkRed))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Financial summary
            BalanceRow("Income", "¥ 0.00", ChalkGreen)
            BalanceRow("Expense", "¥ 0.00", ChalkRed)
            
            DottedDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            BalanceRow("Balance", "¥ 0.00", ChalkBlue)

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChalkButton("+ Expense", onClick = { })
                ChalkButton("+ Income", onClick = { })
            }
            
            Text(
                "** Rotate device to view reports **",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun BalanceRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = ChalkWhite, fontSize = 24.sp, fontFamily = FontFamily.Cursive)
        Text(value, color = color, fontSize = 24.sp, fontFamily = FontFamily.Cursive)
    }
}
