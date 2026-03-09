package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.ui.components.CategoryRow
import net.micode.spendingtracker.ui.components.SectionHeader
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText

@Composable
fun AddCategoryScreen(
    onClose: () -> Unit,
    onDone: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    val labelBlue = Color(0xFF1976D2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeHeader)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = DarkBrownText)
            }
            Text(
                text = "Done",
                color = DarkBrownText,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickable { 
                        if (categoryName.isNotBlank()) {
                            onDone(categoryName)
                        } else {
                            onClose()
                        }
                    }
            )
        }

        SectionHeader(title = "Category Details")

        Column(modifier = Modifier.fillMaxWidth()) {
            CategoryRow(label = "Name", labelColor = labelBlue) {
                BasicTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    textStyle = TextStyle(fontSize = 16.sp, color = DarkBrownText),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (categoryName.isEmpty()) {
                            Text("Name", color = Color.LightGray, fontSize = 16.sp)
                        }
                        innerTextField()
                    }
                )
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

            CategoryRow(label = "Icon", labelColor = labelBlue) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Sell,
                        contentDescription = "Current Icon",
                        tint = DarkBrownText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = "Clear Icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

            CategoryRow(label = "Chart Colour", labelColor = labelBlue) {
                Text(text = "Not Entered", color = Color.LightGray, fontSize = 16.sp)
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
        }
    }
}
