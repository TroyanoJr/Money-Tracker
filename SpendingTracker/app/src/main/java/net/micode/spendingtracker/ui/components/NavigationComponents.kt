package net.micode.spendingtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    onSettingsClick: () -> Unit,
    selectedPeriod: Period,
    selectedDate: Long,
    onPeriodSelected: (Period) -> Unit,
    onDateSelected: (Long) -> Unit,
    isSearchActive: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onToggleSearch: (Boolean) -> Unit = {},
    showSearchOption: Boolean = false
) {
    var showPeriodMenu by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    
    val dateFormatter = remember(selectedPeriod) {
        when (selectedPeriod) {
            Period.DAY -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            Period.WEEK -> SimpleDateFormat("'Week' w, yyyy", Locale.getDefault())
            Period.MONTH -> SimpleDateFormat("MMM yyyy", Locale.getDefault())
            Period.YEAR -> SimpleDateFormat("yyyy", Locale.getDefault())
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
            if (isSearchActive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onToggleSearch(false) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkBrownText)
                    }
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search transactions", color = DarkBrownText.copy(alpha = 0.5f)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = DarkBrownText,
                            focusedTextColor = DarkBrownText,
                            unfocusedTextColor = DarkBrownText
                        ),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DarkBrownText) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                    )
                }
            } else {
                Box {
                    Box(
                        modifier = Modifier
                            .border(1.dp, DarkBrownText.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .clickable { showPeriodMenu = true }
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(dateText, color = DarkBrownText, fontWeight = FontWeight.Medium)
                    }
                    
                    DropdownMenu(
                        expanded = showPeriodMenu, 
                        onDismissRequest = { showPeriodMenu = false },
                        modifier = Modifier.background(BeigeHeader)
                    ) {
                        Period.values().forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period.name, color = DarkBrownText) },
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
                            onDismissRequest = { showOverflowMenu = false },
                            modifier = Modifier.background(BeigeHeader)
                        ) {
                            if (showSearchOption) {
                                DropdownMenuItem(
                                    text = { Text("Search", color = DarkBrownText) },
                                    onClick = {
                                        showOverflowMenu = false
                                        onToggleSearch(true)
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Settings", color = DarkBrownText) },
                                onClick = {
                                    showOverflowMenu = false
                                    onSettingsClick()
                                }
                            )
                        }
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
                    modifier = Modifier.weight(1f).clickable { 
                        if (!isSearchActive) onTabSelected(index) 
                    }
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
    val color = if (selected) DarkBrownText else DarkBrownText.copy(alpha = 0.4f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Text(text, fontSize = 10.sp, color = color, maxLines = 1, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        if (selected) {
            Box(Modifier.padding(top = 4.dp).height(2.dp).width(40.dp).background(DarkBrownText))
        } else {
            Spacer(Modifier.height(6.dp))
        }
    }
}
