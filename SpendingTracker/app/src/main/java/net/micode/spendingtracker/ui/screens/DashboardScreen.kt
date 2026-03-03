package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import net.micode.spendingtracker.ui.components.TopNavigation

/**
 * Main dashboard screen that hosts the navigation and the swipable pages.
 */
@Composable
fun DashboardScreen() {
    // Manages the pager state, including current page and scrolling behavior.
    val pagerState = rememberPagerState(pageCount = { 4 })
    // Coroutine scope needed to trigger scroll animations.
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // Top navigation bar synchronized with the pager state.
        TopNavigation(
            selectedTabIndex = pagerState.currentPage,
            onTabSelected = { index ->
                // Animates the pager to the selected index when a tab is clicked.
                coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                }
            }
        )

        // HorizontalPager allows users to swipe between different screens.
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f) // Fills the remaining screen space.
        ) { page ->
            // Loads the appropriate screen based on the current page index.
            when (page) {
                0 -> SpendingScreen()
                1 -> TransactionsScreen()
                2 -> CategoriesScreen()
                3 -> AccountsScreen()
            }
        }
    }
}
