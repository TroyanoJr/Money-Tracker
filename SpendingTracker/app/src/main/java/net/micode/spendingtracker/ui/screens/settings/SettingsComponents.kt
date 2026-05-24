package net.micode.spendingtracker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable header for settings sections.
 */
@Composable
fun SettingsSectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.3f))
            .padding(16.dp, 8.dp)
    ) {
        Text(title, color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

/**
 * Reusable row with a title, subtitle, and a toggle switch.
 */
@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = if (enabled) Color(0xFF1976D2) else Color.Gray)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(checked, onCheckedChange, enabled = enabled)
    }
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), 0.5.dp, Color.LightGray)
}

/**
 * Reusable row that is clickable and shows a value.
 */
@Composable
fun SettingsClickableRow(
    title: String,
    value: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled) { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, color = if (enabled) Color(0xFF1976D2) else Color.Gray)
        Text(value, fontSize = 16.sp, color = if (enabled) Color(0xFF1976D2) else Color.Gray)
    }
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), 0.5.dp, Color.LightGray)
}
