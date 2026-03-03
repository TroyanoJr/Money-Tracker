package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText

/**
 * Screen that displays the list of transactions.
 * Currently shows an empty state message when no transactions are present.
 */
@Composable
fun TransactionsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeHeader)
            .padding(16.dp)
    ) {
        // Main content area containing the "Empty State" message.
        Box(
            modifier = Modifier
                .weight(1f) // Fills available space, pushing the bottom bar down.
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Stylized box that matches the app's aesthetic.
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(140.dp)
                    .border(1.5.dp, DarkBrownText.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Press the '+' icon to add your first transaction",
                    color = DarkBrownText.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )
            }
        }

        // Bottom action bar for navigation, sharing, and filtering.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Navigation arrows (Left side).
            Row {
                IconButton(onClick = { }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = DarkBrownText)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = DarkBrownText)
                }
            }
            
            // Contextual actions like Share and Filter (Right side).
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
