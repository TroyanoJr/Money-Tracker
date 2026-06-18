package net.micode.moneytracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.moneytracker.model.Account
import net.micode.moneytracker.ui.components.CategoryRow
import net.micode.moneytracker.ui.components.TwoLevelColorPickerDialog
import net.micode.moneytracker.ui.theme.BeigeHeader
import net.micode.moneytracker.ui.theme.DarkBrownText

/**
 * Screen for adding a new financial account or editing an existing one.
 * Users can specify the account name and associate a color with it.
 * 
 * @param accountToEdit The [Account] to edit, or null if creating a new one.
 * @param onClose Callback to close the screen without saving.
 * @param onDone Callback triggered when the account is saved.
 */
@Composable
fun AddAccountScreen(
    accountToEdit: Account? = null,
    onClose: () -> Unit,
    onDone: (Account) -> Unit
) {
    val colorSaver = Saver<Color?, Int>(
        save = { it?.toArgb() ?: 0 },
        restore = { if (it != 0) Color(it) else null }
    )

    var accountName by rememberSaveable { mutableStateOf(accountToEdit?.name ?: "") }
    var selectedColor by rememberSaveable(stateSaver = colorSaver) { 
        mutableStateOf(accountToEdit?.color?.let { Color(it) } ?: Color(0xFF1976D2)) 
    }
    
    var showColorPicker by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val isNameValid by remember { derivedStateOf { accountName.isNotBlank() } }
    val labelBlue = Color(0xFF1976D2)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { /* Trap gestures to prevent closing when clicking background */ },
        color = BeigeHeader
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with Close and Done actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { focusManager.clearFocus(); onClose() }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = DarkBrownText)
                }
                Text(
                    text = stringResource(R.string.done),
                    color = if (isNameValid) DarkBrownText else DarkBrownText.copy(alpha = 0.3f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clickable(enabled = isNameValid) {
                            if (isNameValid) {
                                val account = accountToEdit?.copy(
                                    name = accountName.trim(),
                                    color = selectedColor?.toArgb() ?: Color.Gray.toArgb()
                                ) ?: Account(
                                    name = accountName.trim(),
                                    color = selectedColor?.toArgb() ?: Color.Gray.toArgb()
                                )
                                focusManager.clearFocus()
                                onDone(account)
                            }
                        }
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                // Account Name Input Row
                CategoryRow(
                    label = stringResource(R.string.name),
                    labelColor = if (isNameValid) labelBlue else Color.Red.copy(alpha = 0.7f)
                ) {
                    BasicTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        textStyle = TextStyle(fontSize = 16.sp, color = DarkBrownText),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (accountName.isEmpty()) {
                                Text(stringResource(R.string.name), color = Color.LightGray, fontSize = 16.sp)
                            }
                            innerTextField()
                        }
                    )
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                // Color Selection Row
                CategoryRow(label = stringResource(R.string.icon_color), labelColor = labelBlue) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { focusManager.clearFocus(); showColorPicker = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 40.dp, height = 20.dp)
                                .background(selectedColor ?: Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        if (selectedColor == null) {
                            Text(stringResource(R.string.not_entered), color = Color.LightGray, fontSize = 16.sp)
                        }
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            }
        }
    }

    // Color Picker Dialog
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
