package net.micode.spendingtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText

@Composable
fun SectionHeader(title: String) {
    val colorScheme = MaterialTheme.colorScheme
    // Optimizamos: Recordamos los colores para evitar cálculos en cada frame de animación
    val dividerColor = remember(colorScheme.outline) { colorScheme.outline.copy(alpha = 0.3f) }
    val bgColor = remember(colorScheme.surfaceVariant) { colorScheme.surfaceVariant.copy(alpha = 0.4f) }
    val textColor = remember(colorScheme.onSurfaceVariant) { colorScheme.onSurfaceVariant.copy(alpha = 0.8f) }
    val iconColor = remember(colorScheme.onSurfaceVariant) { colorScheme.onSurfaceVariant.copy(alpha = 0.4f) }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 0.8.dp, color = dividerColor)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = textColor,
                fontSize = 14.sp,
                style = MaterialTheme.typography.labelMedium
            )
            Icon(
                Icons.Default.HelpOutline,
                contentDescription = "Help",
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        HorizontalDivider(thickness = 0.8.dp, color = dividerColor)
    }
}

@Composable
fun CategoryRow(
    label: String,
    labelColor: Color,
    content: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val dividerColor = remember(colorScheme.outline) { colorScheme.outline.copy(alpha = 0.3f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
                    .padding(vertical = 14.dp, horizontal = 12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = label,
                    color = labelColor,
                    fontSize = 15.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 0.8.dp,
                color = dividerColor
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                content()
            }
        }
        HorizontalDivider(thickness = 0.8.dp, color = dividerColor)
    }
}

@Composable
fun CategoryTabButton(
    text: String,
    selected: Boolean,
    isStart: Boolean,
    onClick: () -> Unit
) {
    // Memorizamos la forma para no crear objetos nuevos innecesariamente
    val shape = remember(isStart) {
        if (isStart) RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
        else RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
    }
    val borderColor = remember(selected) {
        if (selected) Color.Transparent else DarkBrownText.copy(alpha = 0.5f)
    }

    Surface(
        modifier = Modifier
            .width(120.dp)
            .height(40.dp)
            .clickable { onClick() }
            .border(width = 1.dp, color = borderColor, shape = shape),
        color = if (selected) DarkBrownText else Color.Transparent,
        shape = shape
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
fun CategorySelectionToolbar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BeigeHeader)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClearSelection) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Clear", tint = DarkBrownText)
        }
        Text(
            text = "$selectedCount selected",
            color = DarkBrownText,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        )
        if (selectedCount == 1) {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = DarkBrownText)
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DarkBrownText)
        }
    }
}
