package net.micode.moneytracker.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.moneytracker.ui.theme.BeigeHeader
import net.micode.moneytracker.ui.theme.DarkBrownText
import net.micode.moneytracker.util.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarryOverSettingsScreen(
    settingsManager: SettingsManager,
    accountId: Long,
    onBack: () -> Unit
) {
    var carryOverEnabled by remember(accountId) { mutableStateOf(settingsManager.isCarryOverEnabled(accountId)) }
    var positiveOnly by remember(accountId) { mutableStateOf(settingsManager.isCarryOverPositiveOnly(accountId)) }
    var addToIncome by remember(accountId) { mutableStateOf(settingsManager.isCarryOverAddToIncome(accountId)) }
    val budgetModeEnabled = remember(accountId) { settingsManager.isBudgetModeEnabled(accountId) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.carry_over), color = DarkBrownText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = DarkBrownText)
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Outlined.Info, contentDescription = "Info", tint = DarkBrownText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BeigeHeader)
            )
        },
        containerColor = BeigeHeader
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader(stringResource(R.string.balance_carry_over))

            SettingsToggleRow(
                title = stringResource(R.string.carry_over),
                subtitle = stringResource(R.string.carry_over_desc),
                checked = carryOverEnabled
            ) {
                carryOverEnabled = it
                settingsManager.setCarryOverEnabled(accountId, it)
            }

            SettingsToggleRow(
                title = stringResource(R.string.positive_only),
                subtitle = stringResource(R.string.positive_only_desc),
                checked = positiveOnly,
                enabled = carryOverEnabled
            ) {
                positiveOnly = it
                settingsManager.setCarryOverPositiveOnly(accountId, it)
            }

            // Ocultar esta opción si el Budget Mode está activo para evitar redundancia y doble conteo
            if (!budgetModeEnabled) {
                SettingsToggleRow(
                    title = stringResource(R.string.add_to_income),
                    subtitle = stringResource(R.string.add_to_income_desc),
                    checked = addToIncome,
                    enabled = carryOverEnabled
                ) {
                    addToIncome = it
                    settingsManager.setCarryOverAddToIncome(accountId, it)
                }
            }
            
            Text(
                text = stringResource(R.string.carry_over_footer_desc),
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }

    if (showInfoDialog) {
        CarryOverInfoDialog(onDismiss = { showInfoDialog = false })
    }
}

@Composable
fun CarryOverInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.info), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.carry_over_info_main))
                Text(stringResource(R.string.carry_over_info_label), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.carry_over_info_desc))
                Text(stringResource(R.string.positive_only_label), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.positive_only_info_desc))
                Text(stringResource(R.string.add_to_income_label), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.add_to_income_info_desc))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close).uppercase(), color = DarkBrownText, fontWeight = FontWeight.Bold)
            }
        }
    )
}
