package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import net.micode.spendingtracker.model.Category
import net.micode.spendingtracker.model.Transaction
import net.micode.spendingtracker.ui.components.TopNavigation
import net.micode.spendingtracker.viewmodel.TransactionViewModel

/**
 * Main dashboard screen that hosts the navigation and the swipable pages.
 */
@Composable
fun DashboardScreen(viewModel: TransactionViewModel = viewModel()) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    
    // UI States for Categories
    var selectedCategorySubTab by remember { mutableIntStateOf(0) }
    var showAddCategory by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    
    // UI States for Transactions
    var showAddTransaction by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    var initialTransactionType by remember { mutableIntStateOf(0) }

    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()

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
                            transactionToEdit = null
                            initialTransactionType = 0
                            showAddTransaction = true
                        }
                        2 -> {
                            categoryToEdit = null
                            showAddCategory = true
                        }
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
                            transactionToEdit = null
                            initialTransactionType = 0
                            showAddTransaction = true
                        },
                        onAddIncome = {
                            transactionToEdit = null
                            initialTransactionType = 1
                            showAddTransaction = true
                        }
                    )
                    1 -> TransactionsScreen(
                        viewModel = viewModel,
                        onEditTransaction = { transaction ->
                            transactionToEdit = transaction
                            showAddTransaction = true
                        },
                        onDeleteTransactions = { transactions ->
                            viewModel.deleteTransactions(transactions)
                        }
                    )
                    2 -> CategoriesScreen(
                        expenseCategories = expenseCategories,
                        incomeCategories = incomeCategories,
                        selectedSubTab = selectedCategorySubTab,
                        onSubTabChanged = { selectedCategorySubTab = it },
                        onEditCategory = { category ->
                            categoryToEdit = category
                            showAddCategory = true
                        },
                        onDeleteCategories = { categories ->
                            viewModel.deleteCategories(categories)
                        }
                    )
                    3 -> AccountsScreen()
                }
            }
        }

        if (showAddCategory) {
            AddCategoryScreen(
                categoryToEdit = categoryToEdit,
                isExpense = selectedCategorySubTab == 0,
                onClose = { 
                    showAddCategory = false
                    categoryToEdit = null
                },
                onDone = { category ->
                    if (categoryToEdit == null) {
                        viewModel.addCategory(category)
                    } else {
                        viewModel.updateCategory(category)
                    }
                    showAddCategory = false 
                    categoryToEdit = null
                }
            )
        }

        if (showAddTransaction) {
            AddTransactionScreen(
                transactionToEdit = transactionToEdit,
                viewModel = viewModel,
                initialType = initialTransactionType,
                onClose = { 
                    showAddTransaction = false
                    transactionToEdit = null
                },
                onDone = { 
                    showAddTransaction = false
                    transactionToEdit = null
                }
            )
        }
    }
}
