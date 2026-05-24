package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.R
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText

@Composable
fun AccountsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeHeader)
            .padding(16.dp)
    ) {
        // Current account display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.5f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                tint = DarkBrownText,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text("TroyanoJr", fontSize = 18.sp, color = DarkBrownText)
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Footer message localized
        Text(
            text = stringResource(R.string.accounts_info),
            color = DarkBrownText.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp)
        )
    }
}
