package net.micode.spendingtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText

/**
 * Top navigation component that handles tab selection and global actions.
 */
@Composable
fun TopNavigation(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onAddClick: () -> Unit // New callback for the "+" button
) {
    Column(modifier = Modifier.background(BeigeHeader)) {
        // Top bar section containing the month selector and options menu.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Month display box with a simple border.
            Box(
                modifier = Modifier
                    .border(1.dp, Color.Gray)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text("三月", color = DarkBrownText)
            }
            
            // Icons for adding new entries and the overflow menu.
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = DarkBrownText)
                }
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = DarkBrownText)
            }
        }
        
        // Tab row that distributes space equally among the navigation items.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val tabs = listOf(
                "Spending" to Icons.Default.Sell,
                "Transactions" to Icons.AutoMirrored.Filled.Assignment,
                "Categories" to Icons.AutoMirrored.Filled.List,
                "Accounts" to Icons.Default.People
            )
            
            // Loop through each tab to create the TabItem component.
            tabs.forEachIndexed { index, pair ->
                val (label, icon) = pair
                TabItem(
                    text = label,
                    icon = icon,
                    selected = selectedTabIndex == index,
                    modifier = Modifier.weight(1f).clickable { onTabSelected(index) }
                )
            }
        }
    }
}

/**
 * Individual tab item that changes color and shows an indicator when selected.
 */
@Composable
fun TabItem(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    // Determine the color based on the selected state.
    val color = if (selected) DarkBrownText else Color.Gray.copy(alpha = 0.6f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        // Icon for the tab.
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        // Label for the tab.
        Text(text, fontSize = 10.sp, color = color, maxLines = 1)
        // Selection indicator (the line below the text).
        if (selected) {
            Box(
                Modifier
                    .padding(top = 4.dp)
                    .height(2.dp)
                    .width(40.dp)
                    .background(DarkBrownText)
            )
        } else {
            // Empty space to maintain the same layout height for non-selected tabs.
            Spacer(Modifier.height(6.dp))
        }
    }
}
