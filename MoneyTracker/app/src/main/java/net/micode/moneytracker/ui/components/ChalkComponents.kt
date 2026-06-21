package net.micode.moneytracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.moneytracker.ui.theme.ChalkWhite

/**
 * A custom button component with a "chalk-like" outlined aesthetic.
 * 
 * @param text The label text to display on the button.
 * @param onClick Callback triggered when the button is clicked.
 * @param modifier The modifier to be applied to the button.
 */
@Composable
fun ChalkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(2.dp, ChalkWhite),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = ChalkWhite)
    ) {
        Text(
            text = text,
            fontSize = 18.sp
        )
    }
}

/**
 * A custom horizontal divider component that draws a dashed (dotted) line.
 * Mimics a chalk line drawn on a blackboard.
 * 
 * @param modifier The modifier to be applied to the divider.
 * @param color The color of the dotted line.
 */
@Composable
fun DottedDivider(
    modifier: Modifier = Modifier,
    color: Color = ChalkWhite
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}
