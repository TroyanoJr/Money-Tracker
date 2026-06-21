package net.micode.moneytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.micode.moneytracker.R

/**
 * A shared two-level color picker dialog used for selecting colors for categories and accounts.
 * Level 1: Select a primary color.
 * Level 2: Select a specific shade of the chosen primary color.
 * 
 * @param onDismiss Callback triggered when the dialog is closed.
 * @param onColorSelected Callback triggered when a final color is selected.
 */
@Composable
fun TwoLevelColorPickerDialog(
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var currentLevel by rememberSaveable { mutableIntStateOf(1) }
    var selectedPrimaryColor by rememberSaveable(
        stateSaver = Saver<Color?, Int>(
            save = { it?.toArgb() ?: 0 },
            restore = { if (it != 0) Color(it) else null }
        )
    ) { mutableStateOf<Color?>(null) }
    
    var tempSelectedColor by rememberSaveable(
        stateSaver = Saver<Color?, Int>(
            save = { it?.toArgb() ?: 0 },
            restore = { if (it != 0) Color(it) else null }
        )
    ) { mutableStateOf<Color?>(null) }

    val primaries = listOf(
        Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
        Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4),
        Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39),
        Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722),
        Color(0xFF795548), Color(0xFF9E9E9E), Color(0xFF607D8B)
    )

    val shades = remember(selectedPrimaryColor) {
        selectedPrimaryColor?.let { color ->
            listOf(
                color.copy(alpha = 0.1f), color.copy(alpha = 0.2f), color.copy(alpha = 0.4f),
                color.copy(alpha = 0.6f), color.copy(alpha = 0.8f), color,
                Color(android.graphics.Color.HSVToColor(FloatArray(3).apply {
                    android.graphics.Color.colorToHSV(color.toArgb(), this)
                    this[2] *= 0.8f
                })),
                Color(android.graphics.Color.HSVToColor(FloatArray(3).apply {
                    android.graphics.Color.colorToHSV(color.toArgb(), this)
                    this[2] *= 0.6f
                })),
                Color(android.graphics.Color.HSVToColor(FloatArray(3).apply {
                    android.graphics.Color.colorToHSV(color.toArgb(), this)
                    this[2] *= 0.4f
                }))
            )
        } ?: emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentLevel == 2) {
                    IconButton(onClick = { currentLevel = 1 }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
                Text(stringResource(R.string.color_picker), fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.padding(bottom = 16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (currentLevel == 1) Color.Gray else Color.LightGray, CircleShape)
                    )
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (currentLevel == 2) Color.Gray else Color.LightGray, CircleShape)
                    )
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(250.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentLevel == 1) {
                        items(primaries) { color ->
                            ColorCircle(
                                color = color,
                                isSelected = tempSelectedColor == color,
                                onClick = {
                                    selectedPrimaryColor = color
                                    tempSelectedColor = color
                                    currentLevel = 2
                                }
                            )
                        }
                    } else {
                        items(shades) { color ->
                            ColorCircle(
                                color = color,
                                isSelected = tempSelectedColor == color,
                                onClick = { tempSelectedColor = color }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { tempSelectedColor?.let { onColorSelected(it) } }) {
                Text(stringResource(R.string.select), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    )
}

/**
 * A circular color representation used within the color picker.
 */
@Composable
fun ColorCircle(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(color, CircleShape)
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(Icons.Default.Check, null, tint = Color.White)
        }
    }
}
