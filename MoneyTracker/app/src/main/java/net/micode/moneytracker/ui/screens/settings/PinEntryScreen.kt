package net.micode.moneytracker.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.moneytracker.util.SettingsManager
import net.micode.moneytracker.R

/**
 * Screen to enter, setup, or change a 4-digit PIN.
 * @param isChangeMode If true, starts by verifying current PIN before allowing change.
 */
@Composable
fun PinEntryScreen(
    settingsManager: SettingsManager,
    isChangeMode: Boolean = false,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    // 1: Create, 2: Confirm, 3: Verify current
    var step by remember {
        mutableStateOf(
            if (isChangeMode || settingsManager.getPasscode().isNotEmpty()) 3 else 1
        )
    }
    var firstAttemptPin by remember { mutableStateOf("") }
    var currentInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    val title = when(step) { 
        1 -> stringResource(R.string.create_pin)
        2 -> stringResource(R.string.confirm_pin)
        else -> stringResource(R.string.verify_pin)
    }

    Surface(
        modifier = Modifier.fillMaxSize(), 
        color = Color(0xFF1A1A1A)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp), 
            horizontalAlignment = Alignment.CenterHorizontally, 
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title, 
                color = Color.White, 
                fontSize = 32.sp, 
                fontStyle = FontStyle.Italic, 
                fontWeight = FontWeight.Light
            )
            
            Text(
                text = stringResource(R.string.pin_chalk_desc),
                color = Color.Gray, 
                fontSize = 14.sp, 
                fontStyle = FontStyle.Italic, 
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            // PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp), 
                modifier = Modifier.padding(bottom = 64.dp)
            ) {
                repeat(4) { index ->
                    val isFilled = index < currentInput.length
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(1.dp, Color.Gray, CircleShape)
                            .padding(4.dp)
                            .background(
                                if (isFilled) Color.White.copy(alpha = 0.8f) else Color.Transparent, 
                                CircleShape
                            )
                    )
                }
            }

            // Keypad
            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "DEL")
            Column(
                modifier = Modifier.width(280.dp), 
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0 until 3) {
                            val key = keys[row * 3 + col]
                            if (key.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clickable {
                                            if (key == "DEL") { 
                                                if (currentInput.isNotEmpty()) currentInput = currentInput.dropLast(1) 
                                            } else if (currentInput.length < 4) {
                                                currentInput += key
                                                if (currentInput.length == 4) {
                                                    when(step) {
                                                        1 -> { 
                                                            firstAttemptPin = currentInput
                                                            currentInput = ""
                                                            step = 2 
                                                        }
                                                        2 -> { 
                                                            if (currentInput == firstAttemptPin) { 
                                                                settingsManager.setPasscode(currentInput)
                                                                onSuccess() 
                                                            } else { 
                                                                Toast.makeText(context, context.getString(R.string.pin_mismatch), Toast.LENGTH_SHORT).show()
                                                                currentInput = ""
                                                                step = 1 
                                                            } 
                                                        }
                                                        3 -> { 
                                                            if (currentInput == settingsManager.getPasscode()) {
                                                                if (isChangeMode) {
                                                                    Toast.makeText(context, context.getString(R.string.pin_verified), Toast.LENGTH_SHORT).show()
                                                                    currentInput = ""
                                                                    step = 1 // Proceed to create new PIN
                                                                } else {
                                                                    onSuccess()
                                                                }
                                                            } else { 
                                                                Toast.makeText(context, context.getString(R.string.pin_wrong), Toast.LENGTH_SHORT).show()
                                                                currentInput = "" 
                                                            } 
                                                        }
                                                    }
                                                }
                                            }
                                        }, 
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (key == "DEL") {
                                        Icon(Icons.AutoMirrored.Filled.Backspace, null, tint = Color.White, modifier = Modifier.size(32.dp))
                                    } else {
                                        Text(
                                            text = key, 
                                            color = Color.White, 
                                            fontSize = 36.sp, 
                                            fontStyle = FontStyle.Italic, 
                                            fontWeight = FontWeight.Light
                                        )
                                    }
                                }
                            } else {
                                Spacer(Modifier.size(80.dp))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.cancel), color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}
