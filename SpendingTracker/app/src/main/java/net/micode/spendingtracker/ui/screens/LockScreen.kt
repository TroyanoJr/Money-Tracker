package net.micode.spendingtracker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.R
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
            text = stringResource(R.string.enter_pin),
            color = ChalkWhite,
            fontSize = 32.sp,
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = stringResource(R.string.pin_chalk_desc),
            color = ChalkWhite.copy(alpha = 0.4f),
            fontSize = 16.sp,
            fontFamily = FontFamily.Cursive
        )

        Spacer(modifier = Modifier.height(56.dp))

        PinInputDisplay(pin = enteredPin, isError = isError)

        Spacer(modifier = Modifier.height(64.dp))

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
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            val digit = pin.getOrNull(index)
            val color by animateColorAsState(
                targetValue = if (isError) Color(0xFFE57373) else ChalkWhite,
                animationSpec = spring()
            )
            val dotSize by animateDpAsState(if (digit != null) 18.dp else 0.dp)

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .drawBehind {
                        drawCircle(
                            color = color.copy(alpha = if (digit != null) 0.6f else 0.2f),
                            radius = 20.dp.toPx(),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (digit != null) {
                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(color)
                    )
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

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    KeypadButton(key, onKeyPress, onDelete)
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    key: String,
    onKeyPress: (String) -> Unit,
    onDelete: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = Modifier
            .size(85.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = key.isNotEmpty()
            ) {
                if (key == "DEL") onDelete() else onKeyPress(key)
            },
        contentAlignment = Alignment.Center
    ) {
        if (key == "DEL") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = null,
                tint = ChalkWhite,
                modifier = Modifier.size(32.dp)
            )
        } else if (key.isNotEmpty()) {
            Text(
                text = key,
                color = ChalkWhite,
                fontSize = 38.sp,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
