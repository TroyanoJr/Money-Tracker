package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.ui.theme.BlackboardBlack
import net.micode.spendingtracker.ui.theme.ChalkWhite

@Composable
fun LockScreen(
    correctPin: String,
    onUnlocked: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackboardBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter PIN",
            color = ChalkWhite,
            fontSize = 32.sp,
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Identical PIN Display (4 Chalk Boxes)
        PinInputDisplay(pin = enteredPin, isError = isError)

        Spacer(modifier = Modifier.height(64.dp))

        // Reusable Numeric Keypad
        ChalkNumericKeypad(
            onKeyPress = { key ->
                if (enteredPin.length < 4) {
                    enteredPin += key
                    isError = false
                    if (enteredPin.length == 4) {
                        if (enteredPin == correctPin) {
                            onUnlocked()
                        } else {
                            isError = true
                            enteredPin = ""
                        }
                    }
                }
            },
            onDelete = {
                if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                isError = false
            }
        )
    }
}

@Composable
fun PinInputDisplay(pin: String, isError: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            val digit = pin.getOrNull(index)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .border(
                        width = 2.dp,
                        color = if (isError) Color.Red else ChalkWhite.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(
                        if (digit != null) ChalkWhite.copy(alpha = 0.1f) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (digit != null) {
                    // Muestra un punto o asterisco estilo tiza
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(ChalkWhite))
                }
            }
        }
    }
}

@Composable
fun ChalkNumericKeypad(
    onKeyPress: (String) -> Unit,
    onDelete: () -> Unit
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .clickable(enabled = key.isNotEmpty()) {
                                if (key == "DEL") onDelete() else onKeyPress(key)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "DEL") {
                            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete", tint = ChalkWhite)
                        } else if (key.isNotEmpty()) {
                            Text(
                                text = key,
                                color = ChalkWhite,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
