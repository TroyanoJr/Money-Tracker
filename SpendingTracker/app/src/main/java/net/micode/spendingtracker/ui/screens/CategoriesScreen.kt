package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText

/**
 * Screen that displays a list of financial categories.
 * It features a sub-navigation (Expense/Income) using a swipable pager.
 */
@Composable
fun CategoriesScreen(
    expenseCategories: List<Pair<String, ImageVector>>,
    incomeCategories: List<Pair<String, ImageVector>>,
    selectedSubTab: Int,
    onSubTabChanged: (Int) -> Unit
) {
    // Pager state for swiping between Expense and Income lists.
    val pagerState = rememberPagerState(initialPage = selectedSubTab, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    // Sync external state changes to pager (e.g., when clicking tabs).
    LaunchedEffect(selectedSubTab) {
        if (pagerState.currentPage != selectedSubTab) {
            pagerState.animateScrollToPage(selectedSubTab)
        }
    }

    // Sync pager changes back to the parent state (needed for the "+" button logic).
    LaunchedEffect(pagerState.currentPage) {
        onSubTabChanged(pagerState.currentPage)
    }

    Column(modifier = Modifier.fillMaxSize().background(BeigeHeader)) {
        // Sub-Navigation Header: EXPENSE | INCOME
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Expense Tab Button
            CategoryTabButton(
                text = "EXPENSE",
                selected = pagerState.currentPage == 0,
                isStart = true,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
            )
            // Income Tab Button
            CategoryTabButton(
                text = "INCOME",
                selected = pagerState.currentPage == 1,
                isStart = false,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
            )
        }
        
        // Swipable area for the categories list.
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val listToDisplay = if (page == 0) expenseCategories else incomeCategories
            
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(listToDisplay) { pair ->
                    val (name, icon) = pair
                    CategoryItem(name, icon)
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        thickness = 0.5.dp, 
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

/**
 * Custom tab button for the Expense/Income selector.
 */
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
                if (!selected) Modifier.border(
                    1.dp, 
                    DarkBrownText, 
                    if (isStart) RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp) 
                    else RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                ) else Modifier
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

/**
 * Component for a single category item in the list.
 */
@Composable
fun CategoryItem(name: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DarkBrownText,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = name,
            color = DarkBrownText,
            fontSize = 16.sp
        )
    }
}
