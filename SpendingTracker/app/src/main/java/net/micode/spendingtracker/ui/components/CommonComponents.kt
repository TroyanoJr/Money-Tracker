package net.micode.spendingtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.ui.theme.DarkBrownText

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
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
        Box(
            modifier = Modifier
                .width(110.dp)
                .fillMaxHeight()
                .padding(end = 12.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(text = label, color = labelColor, fontSize = 14.sp)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            content()
        }
    }
}
