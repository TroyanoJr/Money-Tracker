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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText

@Composable
fun SectionHeader(title: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Trazo superior para definir la sección
        HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 14.sp,
                style = MaterialTheme.typography.labelMedium
            )
            Icon(
                Icons.Default.HelpOutline,
                contentDescription = "Help",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
        // Trazo inferior de la cabecera
        HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    }
}

@Composable
fun CategoryRow(
    label: String,
    labelColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface) // Fondo sólido para claridad
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min), // Clave para el VerticalDivider continuo
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ETIQUETA (Izquierda)
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

            // DIVISOR VERTICAL (Crea la armonía de la rejilla)
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 0.8.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            // CONTENIDO (Derecha)
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
        // DIVISOR HORIZONTAL (Borde de la fila)
        HorizontalDivider(
            thickness = 0.8.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
    val shape = if (isStart) RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp) 
                else RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
    Surface(
        modifier = Modifier
            .width(120.dp)
            .height(40.dp)
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else DarkBrownText.copy(alpha = 0.5f),
                shape = shape
            ),
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Clear Selection", tint = DarkBrownText)
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
