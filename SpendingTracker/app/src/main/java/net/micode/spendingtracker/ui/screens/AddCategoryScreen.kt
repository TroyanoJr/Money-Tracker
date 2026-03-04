package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText

/**
 * Screen for adding or editing a category.
 * Matches the "Category Details" design with Name, Icon, and Color fields.
 */
@Composable
fun AddCategoryScreen(
    onClose: () -> Unit,
    onDone: (String) -> Unit // Modified to pass back the category name to the caller
) {
    // Local state for the category name input field
    var categoryName by remember { mutableStateOf("") }
    val labelBlue = Color(0xFF1976D2) // Specific blue for labels like "Name", "Icon"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeHeader)
    ) {
        // Top Toolbar: [X] Done
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
                        // Only trigger onDone if the name is not empty
                        if (categoryName.isNotBlank()) {
                            onDone(categoryName)
                        } else {
                            onClose()
                        }
                    }
            )
        }

        // Section Header: Category Details [?]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray.copy(alpha = 0.3f))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Category Details",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Icon(
                Icons.Default.HelpOutline,
                contentDescription = "Help",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }

        // Form: Table-like structure
        Column(modifier = Modifier.fillMaxWidth()) {
            // Row 1: Name input field
            CategoryRow(label = "Name", labelColor = labelBlue) {
                BasicTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    textStyle = TextStyle(fontSize = 16.sp, color = DarkBrownText),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        // Show placeholder if text is empty
                        if (categoryName.isEmpty()) {
                            Text("Name", color = Color.LightGray, fontSize = 16.sp)
                        }
                        innerTextField()
                    }
                )
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

            // Row 2: Icon selector placeholder
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

            // Row 3: Chart Colour selector placeholder
            CategoryRow(label = "Chart Colour", labelColor = labelBlue) {
                Text(text = "Not Entered", color = Color.LightGray, fontSize = 16.sp)
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
        }
    }
}

/**
 * Helper component to create the form rows with label on the left and content on the right.
 */
@Composable
fun CategoryRow(
    label: String,
    labelColor: Color,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Labels aligned to the end
        Box(
            modifier = Modifier
                .width(110.dp)
                .fillMaxHeight()
                .border(0.2.dp, Color.LightGray)
                .padding(end = 12.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(text = label, color = labelColor, fontSize = 14.sp)
        }
        // Right Column: Content/Input area aligned to the start
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(0.2.dp, Color.LightGray)
                .padding(start = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            content()
        }
    }
}
