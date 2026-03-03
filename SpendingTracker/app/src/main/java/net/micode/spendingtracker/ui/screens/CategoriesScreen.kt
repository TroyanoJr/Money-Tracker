package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText

@Composable
fun CategoriesScreen() {
    val categories = listOf(
        "Eating Out" to Icons.Default.Restaurant,
        "Shopping" to Icons.Default.ShoppingCart,
        "Travel" to Icons.Default.DirectionsBus,
        "General" to Icons.Default.Sell,
        "Wifi" to Icons.Default.Wifi,
        "Water" to Icons.Default.WaterDrop,
        "School" to Icons.Default.School,
        "Clothes" to Icons.Default.Checkroom
    )

    Column(modifier = Modifier.fillMaxSize().background(BeigeHeader)) {
        // Expense/Income Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                color = DarkBrownText,
                shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("EXPENSE", color = Color.White, fontSize = 14.sp)
                }
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .border(1.dp, DarkBrownText, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)),
                color = Color.Transparent,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("INCOME", color = DarkBrownText, fontSize = 14.sp)
                }
            }
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(categories) { pair ->
                val (name, icon) = pair
                CategoryItem(name, icon)
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun CategoryItem(name: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = DarkBrownText, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = name, color = DarkBrownText, fontSize = 18.sp)
    }
}
