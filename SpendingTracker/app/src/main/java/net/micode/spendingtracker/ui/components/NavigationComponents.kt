package net.micode.spendingtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.viewmodel.Period
import java.text.SimpleDateFormat
import java.util.*

/**
 * Top navigation component that handles tab selection and global actions.
 */
@Composable
fun TopNavigation(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit, // New callback
    selectedPeriod: Period,
    selectedDate: Long,
    onPeriodSelected: (Period) -> Unit
) {
    var showPeriodMenu by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    
    val dateFormatter = remember(selectedPeriod) {
        when (selectedPeriod) {
            Period.DAY -> SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            Period.WEEK -> SimpleDateFormat("'Week' w, yyyy", Locale.CHINA)
            Period.MONTH -> SimpleDateFormat("MM月", Locale.CHINA)
            Period.YEAR -> SimpleDateFormat("yyyy", Locale.CHINA)
        }
    }
    
    val dateText = dateFormatter.format(Date(selectedDate))

    Column(modifier = Modifier.background(BeigeHeader)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.Gray)
                        .clickable { showPeriodMenu = true }
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(dateText, color = DarkBrownText)
                }
                
                DropdownMenu(expanded = showPeriodMenu, onDismissRequest = { showPeriodMenu = false }) {
                    Period.values().forEach { period ->
                        DropdownMenuItem(
                            text = { Text(period.name) },
                            onClick = {
                                onPeriodSelected(period)
                                showPeriodMenu = false
                            }
                        )
                    }
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = DarkBrownText)
                }
                Box {
                    IconButton(onClick = { showOverflowMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = DarkBrownText)
                    }
                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                showOverflowMenu = false
                                onSettingsClick()
                            }
                        )
                    }
                }
            }
        }
        
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

@Composable
fun TabItem(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (selected) DarkBrownText else Color.Gray.copy(alpha = 0.6f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Text(text, fontSize = 10.sp, color = color, maxLines = 1)
        if (selected) {
            Box(Modifier.padding(top = 4.dp).height(2.dp).width(40.dp).background(DarkBrownText))
        } else {
            Spacer(Modifier.height(6.dp))
        }
    }
}
