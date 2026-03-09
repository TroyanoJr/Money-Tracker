package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import net.micode.spendingtracker.ui.components.TopNavigation
import net.micode.spendingtracker.viewmodel.TransactionViewModel

/**
 * Main dashboard screen that hosts the navigation and the swipable pages.
 */
@Composable
fun DashboardScreen(viewModel: TransactionViewModel = viewModel()) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    
    // State to track which sub-tab (Expense=0, Income=1) is active in CategoriesScreen.
    var selectedCategorySubTab by remember { mutableIntStateOf(0) }

    // Visibility states for the overlay screens.
    var showAddCategory by remember { mutableStateOf(false) }
    var showAddTransaction by remember { mutableStateOf(false) }
    var initialTransactionType by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopNavigation(
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                onAddClick = {
                    when (pagerState.currentPage) {
                        1 -> {
                            initialTransactionType = 0
                            showAddTransaction = true
                        }
                        2 -> showAddCategory = true
                    }
                }
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> SpendingScreen(
                        viewModel = viewModel,
                        onAddExpense = {
                            initialTransactionType = 0
                            showAddTransaction = true
                        },
                        onAddIncome = {
                            initialTransactionType = 1
                            showAddTransaction = true
                        }
                    )
                    1 -> TransactionsScreen(viewModel)
                    2 -> CategoriesScreen(
                        expenseCategories = viewModel.expenseCategories,
                        incomeCategories = viewModel.incomeCategories,
                        selectedSubTab = selectedCategorySubTab,
                        onSubTabChanged = { selectedCategorySubTab = it }
                    )
                    3 -> AccountsScreen()
                }
            }
        }

        if (showAddCategory) {
            AddCategoryScreen(
                onClose = { showAddCategory = false },
                onDone = { newName ->
                    if (selectedCategorySubTab == 0) {
                        viewModel.expenseCategories.add(newName to Icons.Default.Sell)
                    } else {
                        viewModel.incomeCategories.add(newName to Icons.Default.Sell)
                    }
                    showAddCategory = false 
                }
            )
        }

        if (showAddTransaction) {
            AddTransactionScreen(
                viewModel = viewModel,
                initialType = initialTransactionType,
                onClose = { showAddTransaction = false },
                onDone = { showAddTransaction = false }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardPreview() {
    MaterialTheme {
        Surface {
            DashboardScreen()
        }
    }
}
