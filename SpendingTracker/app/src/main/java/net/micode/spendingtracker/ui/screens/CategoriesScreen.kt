package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.micode.spendingtracker.model.Category
import net.micode.spendingtracker.ui.components.CategorySelectionToolbar
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText

/**
 * Screen that displays a list of financial categories.
 * Supports multi-selection, deletion and editing.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoriesScreen(
    expenseCategories: List<Category>,
    incomeCategories: List<Category>,
    selectedSubTab: Int,
    onSubTabChanged: (Int) -> Unit,
    onEditCategory: (Category) -> Unit,
    onDeleteCategories: (List<Category>) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = selectedSubTab, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    
    // Estado para la selección múltiple
    var selectedCategoryIds by remember { mutableStateOf(setOf<Long>()) }
    
    // Estado para el diálogo de confirmación de borrado
    var showDeleteDialog by remember { mutableStateOf(false) }
    var categoriesToDelete by remember { mutableStateOf<List<Category>>(emptyList()) }

    LaunchedEffect(selectedSubTab) {
        if (pagerState.currentPage != selectedSubTab) {
            pagerState.animateScrollToPage(selectedSubTab)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        onSubTabChanged(pagerState.currentPage)
        selectedCategoryIds = emptySet() // Limpiar selección al cambiar de pestaña
    }

    Column(modifier = Modifier.fillMaxSize().background(BeigeHeader)) {
        // Barra de herramientas dinámica cuando hay selección
        if (selectedCategoryIds.isNotEmpty()) {
            CategorySelectionToolbar(
                selectedCount = selectedCategoryIds.size,
                onClearSelection = { selectedCategoryIds = emptySet() },
                onEdit = {
                    val category = (expenseCategories + incomeCategories).find { it.id == selectedCategoryIds.first() }
                    category?.let { onEditCategory(it) }
                    selectedCategoryIds = emptySet()
                },
                onDelete = {
                    categoriesToDelete = (expenseCategories + incomeCategories).filter { it.id in selectedCategoryIds }
                    showDeleteDialog = true
                }
            )
        }

        // Sub-Navigation Header: EXPENSE | INCOME
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            CategoryTabButton(
                text = "EXPENSE",
                selected = pagerState.currentPage == 0,
                isStart = true,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
            )
            CategoryTabButton(
                text = "INCOME",
                selected = pagerState.currentPage == 1,
                isStart = false,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
            )
        }
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val listToDisplay = if (page == 0) expenseCategories else incomeCategories
            
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(listToDisplay) { category ->
                    val isSelected = selectedCategoryIds.contains(category.id)
                    CategoryItem(
                        category = category,
                        isSelected = isSelected,
                        onClick = {
                            if (selectedCategoryIds.isEmpty()) {
                                onEditCategory(category)
                            } else {
                                selectedCategoryIds = if (isSelected) {
                                    selectedCategoryIds - category.id
                                } else {
                                    selectedCategoryIds + category.id
                                }
                            }
                        },
                        onLongClick = {
                            selectedCategoryIds = selectedCategoryIds + category.id
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        thickness = 0.5.dp, 
                        color = Color.LightGray
                    )
                }
            }
        }
    }

    // Diálogo de confirmación de borrado
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { 
                Text(
                    text = "Delete Category", 
                    color = DarkBrownText,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                ) 
            },
            text = { 
                Text(
                    text = "Are you sure you want to delete the selected categories?",
                    color = DarkBrownText
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCategories(categoriesToDelete)
                        selectedCategoryIds = emptySet()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color.Red, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = DarkBrownText)
                }
            },
            containerColor = BeigeHeader
        )
    }
}

@Composable
fun CategoryTabButton(
    text: String,
    selected: Boolean,
    isStart: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(120.dp)
            .height(40.dp)
            .clickable { onClick() }
            .then(
                if (!selected) {
                    Modifier.border(
                        1.dp, 
                        DarkBrownText, 
                        if (isStart) RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp) 
                        else RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                    )
                } else Modifier
            ),
        color = if (selected) DarkBrownText else Color.Transparent,
        shape = if (isStart) RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp) 
                else RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text, 
                color = if (selected) Color.White else DarkBrownText, 
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Color(0xFFE0F7FA) else Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Sell,
            contentDescription = null,
            tint = DarkBrownText,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = category.name,
            color = DarkBrownText,
            fontSize = 16.sp
        )
    }
}
