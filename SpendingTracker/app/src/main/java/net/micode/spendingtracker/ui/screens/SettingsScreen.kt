package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.util.SettingsManager
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    
    var autoCaptureEnabled by remember { 
        mutableStateOf(settingsManager.isAutoCaptureEnabled()) 
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeHeader)
    ) {
        // Custom Top Bar similar to reference
        TopAppBar(
            title = { Text("Settings", color = DarkBrownText, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkBrownText)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BeigeHeader)
        )

        // Sections
        Column(modifier = Modifier.fillMaxWidth()) {
            SettingsSectionHeader(title = "Automation")
            
            SettingsToggleRow(
                title = "Automatic Notification Capture",
                subtitle = "Detect and classify payments from other apps automatically",
                enabled = autoCaptureEnabled,
                onToggle = {
                    autoCaptureEnabled = it
                    settingsManager.setAutoCaptureEnabled(it)
                }
            )
            
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!enabled) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = DarkBrownText, fontSize = 16.sp)
            Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DarkBrownText,
                checkedTrackColor = DarkBrownText.copy(alpha = 0.5f)
            )
        )
    }
}
