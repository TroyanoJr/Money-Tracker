package net.micode.moneytracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DarkBrownText,
    onPrimary = Color.White,
    primaryContainer = BeigeHeader,
    onPrimaryContainer = DarkBrownText,
    secondary = ChalkGreen,
    onSecondary = Color.White,
    background = BeigeHeader,
    onBackground = DarkBrownText,
    surface = Color.White,
    onSurface = DarkBrownText,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color.Gray,
    outline = DarkBrownText.copy(alpha = 0.5f)
)

@Composable
fun SpendingTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // For now we use the same palette for both to maintain consistency
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
