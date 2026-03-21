package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.model.Category
import net.micode.spendingtracker.ui.components.CategoryRow
import net.micode.spendingtracker.ui.components.SectionHeader
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText

@Composable
fun AddCategoryScreen(
    categoryToEdit: Category? = null,
    isExpense: Boolean = true,
    onClose: () -> Unit,
    onDone: (Category) -> Unit
) {
    // Saver personalizado para el tipo Color (lo guarda como Int ARGB)
    val colorSaver = Saver<Color?, Int>(
        save = { it?.toArgb() ?: 0 },
        restore = { if (it != 0) Color(it) else null }
    )

    var categoryName by rememberSaveable { mutableStateOf(categoryToEdit?.name ?: "") }
    var selectedColor by rememberSaveable(stateSaver = colorSaver) { 
        mutableStateOf(categoryToEdit?.color?.let { Color(it) }) 
    }
    var showColorPicker by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Validación del nombre
    val isNameValid by remember { derivedStateOf { categoryName.isNotBlank() } }

    val labelBlue = Color(0xFF1976D2)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { }, // Bloquea toques al fondo
        color = BeigeHeader
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    focusManager.clearFocus()
                    onClose()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = DarkBrownText)
                }
                Text(
                    text = "Done",
                    color = if (isNameValid) DarkBrownText else DarkBrownText.copy(alpha = 0.3f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clickable(enabled = isNameValid) { 
                            if (isNameValid) {
                                val category = categoryToEdit?.copy(
                                    name = categoryName.trim(),
                                    color = selectedColor?.toArgb()
                                ) ?: Category(
                                    name = categoryName.trim(),
                                    iconName = "Sell",
                                    isExpense = isExpense,
                                    color = selectedColor?.toArgb()
                                )
                                focusManager.clearFocus()
                                onDone(category)
                            }
                        }
                )
            }

            SectionHeader(title = "Category Details")

            Column(modifier = Modifier.fillMaxWidth()) {
                // Feedback visual: la etiqueta cambia a rojo si está vacío
                CategoryRow(
                    label = "Name", 
                    labelColor = if (isNameValid) labelBlue else Color.Red.copy(alpha = 0.7f)
                ) {
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
                    Row(
                        modifier = Modifier.clickable { focusManager.clearFocus() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { 
                                    focusManager.clearFocus()
                                    showColorPicker = true 
                                }
                        ) {
                            if (selectedColor == null) {
                                Text(text = "Not Entered", color = Color.LightGray, fontSize = 16.sp)
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(width = 40.dp, height = 20.dp)
                                        .background(selectedColor!!)
                                )
                            }
                        }
                        
                        if (selectedColor != null) {
                            IconButton(
                                onClick = { 
                                    focusManager.clearFocus()
                                    selectedColor = null 
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Clear Color",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            }
        }
    }

    if (showColorPicker) {
        TwoLevelColorPickerDialog(
            onDismiss = { showColorPicker = false },
            onColorSelected = { color ->
                selectedColor = color
                showColorPicker = false
            }
        )
    }
}

@Composable
fun TwoLevelColorPickerDialog(
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var currentLevel by rememberSaveable { mutableIntStateOf(1) }
    var selectedPrimaryColor by rememberSaveable(stateSaver = Saver<Color?, Int>(save = { it?.toArgb() ?: 0 }, restore = { if (it != 0) Color(it) else null })) { mutableStateOf<Color?>(null) }
    var tempSelectedColor by rememberSaveable(stateSaver = Saver<Color?, Int>(save = { it?.toArgb() ?: 0 }, restore = { if (it != 0) Color(it) else null })) { mutableStateOf<Color?>(null) }

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                Text("Color Picker", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.padding(bottom = 16.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(if (currentLevel == 1) Color.Gray else Color.LightGray, CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Box(modifier = Modifier.size(8.dp).background(if (currentLevel == 2) Color.Gray else Color.LightGray, CircleShape))
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
                Text("SELECT", color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    )
}

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
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
        }
    }
}
