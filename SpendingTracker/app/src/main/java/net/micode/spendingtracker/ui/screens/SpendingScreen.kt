package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.ui.components.ChalkButton
import net.micode.spendingtracker.ui.components.DottedDivider
import net.micode.spendingtracker.ui.theme.*

/**
 * Main screen for the Spending Tracker.
 * Organizes the blackboard-style UI elements vertically.
 */
@Composable
fun SpendingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackboardBlack) // Sets the main dark background for the blackboard effect
    ) {
        // Navigation header at the top
        TopNavigation()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Period selector: Navigation between months or days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AutoMirrored icons handle Right-to-Left (RTL) layouts automatically
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = ChalkWhite)
                }
                Spacer(Modifier.width(20.dp))
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = ChalkWhite)
                }
            }

            // Visual balance bar: Shows the ratio of income vs expenses using weight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                // weight determines the proportional width of each segment
                Box(modifier = Modifier.weight(0.6f).fillMaxHeight().background(ChalkGreen))
                Box(modifier = Modifier.weight(0.4f).fillMaxHeight().background(ChalkRed))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Financial summary section
            BalanceRow("Income", "¥ 0.00", ChalkGreen)
            BalanceRow("Expense", "¥ 0.00", ChalkRed)
            
            // Custom divider simulating a chalk line
            DottedDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            BalanceRow("Balance", "¥ 0.00", ChalkBlue)

            // Flexible spacer to push action buttons to the bottom of the screen
            Spacer(modifier = Modifier.weight(1f))

            // Action buttons for adding new transactions
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

/**
 * Top navigation component containing the month selector and main tabs.
 */
@Composable
fun TopNavigation() {
    Column(modifier = Modifier.background(BeigeHeader)) {
        // Top bar with month display and options menu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, Color.Gray)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text("三月", color = DarkBrownText)
            }
            Icon(Icons.Default.MoreVert, contentDescription = null, tint = DarkBrownText)
        }
        
        // Navigation tabs for different app sections
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TabItem("Spending", Icons.Default.ShoppingCart, selected = true)
            TabItem("Transactions", Icons.AutoMirrored.Filled.List)
            TabItem("Categories", Icons.Default.Menu)
            TabItem("Accounts", Icons.Default.Person)
        }
    }
}

/**
 * Individual tab item with an icon and text.
 * Shows an indicator line if selected.
 */
@Composable
fun TabItem(text: String, icon: ImageVector, selected: Boolean = false) {
    val color = if (selected) DarkBrownText else Color.Gray
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Text(text, fontSize = 11.sp, color = color)
        if (selected) {
            // Horizontal line indicating the active selection
            Box(Modifier.height(2.dp).width(40.dp).background(DarkBrownText))
        }
    }
}

/**
 * Row displaying a label and its corresponding monetary value.
 */
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

/**
 * Preview function to visualize the screen in Android Studio's Design tab.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SpendingScreenPreview() {
    MaterialTheme {
        SpendingScreen()
    }
}
