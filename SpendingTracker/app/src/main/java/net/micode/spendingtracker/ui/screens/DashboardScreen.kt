package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import net.micode.spendingtracker.ui.components.TopNavigation

/**
 * Main dashboard screen that hosts the navigation and the swipable pages.
 * Acts as the single source of truth for dynamic data like categories and UI states.
 */
@Composable
fun DashboardScreen() {
    // Pager state to manage horizontal navigation between main tabs (Spending, Transactions, etc.)
    val pagerState = rememberPagerState(pageCount = { 4 })
    // Coroutine scope used for triggering pager animations
    val coroutineScope = rememberCoroutineScope()
    
    // Separate reactive lists for Expense and Income categories.
    // These are initialized with default values but can be modified dynamically.
    val expenseCategories = remember { 
        mutableStateListOf(
            "Eating Out" to Icons.Default.Restaurant,
            "Shopping" to Icons.Default.ShoppingCart,
            "Travel" to Icons.Default.DirectionsBus,
            "General" to Icons.Default.Sell,
            "Wifi" to Icons.Default.Wifi,
            "Water" to Icons.Default.WaterDrop,
            "School" to Icons.Default.School,
            "Clothes" to Icons.Default.Checkroom
        )
    }
    val incomeCategories = remember {
        mutableStateListOf(
            "Salary" to Icons.Default.Payments,
            "Bonus" to Icons.Default.Star,
            "Investment" to Icons.Default.TrendingUp
        )
    }

    // State to track which sub-tab (Expense=0, Income=1) is active in CategoriesScreen.
    // This allows the "Add" button to know which list to add a new category to.
    var selectedCategorySubTab by remember { mutableIntStateOf(0) }

    // Visibility states for the overlay screens (Add Category and Add Transaction).
    var showAddCategory by remember { mutableStateOf(false) }
    var showAddTransaction by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Shared top navigation component
            TopNavigation(
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                onAddClick = {
                    // Context-aware "Add" button: opens a different screen depending on the active tab.
                    when (pagerState.currentPage) {
                        1 -> showAddTransaction = true // Open transaction editor for Transactions tab
                        2 -> showAddCategory = true    // Open category editor for Categories tab
                    }
                }
            )

            // Main swipable content area
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> SpendingScreen()
                    1 -> TransactionsScreen()
                    2 -> CategoriesScreen(
                        expenseCategories = expenseCategories,
                        incomeCategories = incomeCategories,
                        selectedSubTab = selectedCategorySubTab,
                        onSubTabChanged = { selectedCategorySubTab = it }
                    )
                    3 -> AccountsScreen()
                }
            }
        }

        // Overlay: Screen for creating new categories.
        if (showAddCategory) {
            AddCategoryScreen(
                onClose = { showAddCategory = false },
                onDone = { newName ->
                    // Adds the new category to the currently visible sub-list (Expense or Income).
                    if (selectedCategorySubTab == 0) {
                        expenseCategories.add(newName to Icons.Default.Sell)
                    } else {
                        incomeCategories.add(newName to Icons.Default.Sell)
                    }
                    showAddCategory = false 
                }
            )
        }

        // Overlay: Screen for creating new transactions (matches reference design).
        if (showAddTransaction) {
            AddTransactionScreen(
                onClose = { showAddTransaction = false },
                onDone = {
                    // Placeholder for transaction saving logic.
                    showAddTransaction = false
                }
            )
        }
    }
}

/**
 * Preview for the main Dashboard.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardPreview() {
    MaterialTheme {
        Surface {
            DashboardScreen()
        }
    }
}
