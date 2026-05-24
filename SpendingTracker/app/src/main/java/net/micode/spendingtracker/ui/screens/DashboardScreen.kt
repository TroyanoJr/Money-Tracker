package net.micode.spendingtracker.ui.screens

import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import net.micode.spendingtracker.R
import net.micode.spendingtracker.model.Category
import net.micode.spendingtracker.model.Transaction
import net.micode.spendingtracker.ui.components.BannerAdView
import net.micode.spendingtracker.ui.components.TopNavigation
import net.micode.spendingtracker.viewmodel.TransactionViewModel
import java.io.File

@Composable
fun DashboardScreen(
    viewModel: TransactionViewModel = viewModel(),
    initialTransactionIdToEdit: String? = null
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val pagerState = rememberPagerState(pageCount = { 4 })
    var currentScreen by rememberSaveable { mutableStateOf("dashboard") } 
    
    var selectedCategorySubTab by rememberSaveable { mutableIntStateOf(0) }
    var showAddCategory by rememberSaveable { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    
    var showAddTransaction by rememberSaveable { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    var initialTransactionType by rememberSaveable { mutableIntStateOf(0) }

    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()

    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val transactions by viewModel.transactions.collectAsState()
    LaunchedEffect(initialTransactionIdToEdit, transactions) {
        if (initialTransactionIdToEdit != null && transactions.isNotEmpty()) {
            val tx = transactions.find { it.id == initialTransactionIdToEdit }
            if (tx != null) {
                transactionToEdit = tx
                showAddTransaction = true
            }
        }
    }

    if (isSearchActive) {
        BackHandler {
            isSearchActive = false
            viewModel.setSearchQuery(null)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (isSearchActive && pagerState.currentPage != 1) {
            isSearchActive = false
            viewModel.setSearchQuery(null)
        }
    }

    if (isLandscape) {
        ReportsScreen(viewModel = viewModel)
    } else {
        when (currentScreen) {
            "settings" -> {
                val leaveSettings = {
                    viewModel.refreshCurrency()
                    currentScreen = "dashboard"
                }
                BackHandler(onBack = leaveSettings)
                SettingsScreen(
                    onBack = leaveSettings,
                    onCurrencyChanged = { viewModel.refreshCurrency() }
                )
            }
            "csv_export" -> CsvExportScreen(
                categories = categories.map { it.name },
                onClose = { currentScreen = "dashboard" },
                onExport = { start, end, negate, cat, sort, sep ->
                    coroutineScope.launch {
                        val csvContent = viewModel.generateCsvString(start, end, negate, cat, sort, sep)
                        val file = File(context.cacheDir, "transactions_export.csv")
                        file.writeText(csvContent)
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_csv)))
                        currentScreen = "dashboard"
                    }
                }
            )
            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TopNavigation(
                            selectedTabIndex = pagerState.currentPage,
                            onTabSelected = { index -> coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                            onAddClick = {
                                when (pagerState.currentPage) {
                                    1 -> { transactionToEdit = null; initialTransactionType = 0; showAddTransaction = true }
                                    2 -> { categoryToEdit = null; showAddCategory = true }
                                }
                            },
                            onSettingsClick = { currentScreen = "settings" },
                            selectedPeriod = selectedPeriod,
                            selectedDate = selectedDate,
                            onPeriodSelected = { viewModel.setPeriod(it) },
                            onDateSelected = { viewModel.setDate(it) },
                            isSearchActive = isSearchActive,
                            searchQuery = searchQuery ?: "",
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onToggleSearch = { active -> isSearchActive = active; if (!active) viewModel.setSearchQuery(null) },
                            showSearchOption = pagerState.currentPage == 1
                        )

                        HorizontalPager(
                            state = pagerState, 
                            modifier = Modifier.weight(1f), 
                            userScrollEnabled = !isSearchActive
                        ) { page ->
                            when (page) {
                                0 -> SpendingScreen(viewModel = viewModel, onAddExpense = { transactionToEdit = null; initialTransactionType = 0; showAddTransaction = true }, onAddIncome = { transactionToEdit = null; initialTransactionType = 1; showAddTransaction = true })
                                1 -> TransactionsScreen(viewModel = viewModel, onEditTransaction = { transaction -> transactionToEdit = transaction; showAddTransaction = true }, onDeleteTransactions = { txs -> viewModel.deleteTransactions(txs) }, onExportCsv = { currentScreen = "csv_export" })
                                2 -> CategoriesScreen(expenseCategories = expenseCategories, incomeCategories = incomeCategories, selectedSubTab = selectedCategorySubTab, onSubTabChanged = { selectedCategorySubTab = it }, onEditCategory = { category -> categoryToEdit = category; showAddCategory = true }, onDeleteCategories = { cats -> viewModel.deleteCategories(categories = cats) })
                                3 -> AccountsScreen()
                            }
                        }
                        
                        // Add Banner Ad at the bottom of the main content column
                        BannerAdView(modifier = Modifier.padding(vertical = 4.dp))
                    }

                    if (showAddCategory) {
                        AddCategoryScreen(categoryToEdit = categoryToEdit, isExpense = selectedCategorySubTab == 0, onClose = { showAddCategory = false; categoryToEdit = null }, onDone = { category -> if (categoryToEdit == null) viewModel.addCategory(category) else viewModel.updateCategory(category); showAddCategory = false; categoryToEdit = null })
                    }

                    if (showAddTransaction) {
                        AddTransactionScreen(transactionToEdit = transactionToEdit, viewModel = viewModel, initialType = initialTransactionType, onClose = { showAddTransaction = false; transactionToEdit = null }, onDone = { showAddTransaction = false; transactionToEdit = null })
                    }
                }
            }
        }
    }
}
