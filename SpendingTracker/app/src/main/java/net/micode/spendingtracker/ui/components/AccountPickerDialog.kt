package net.micode.spendingtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.R
import net.micode.spendingtracker.model.Account
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText

/**
 * A dialog that allows the user to switch between different financial accounts.
 * 
 * @param accounts The list of available [Account] objects.
 * @param selectedAccountId The ID of the currently selected account.
 * @param onAccountSelected Callback triggered when an account is selected.
 * @param onDismiss Callback to dismiss the dialog.
 * @param showAllAccountsOption Whether to show an "All Accounts" option in the list.
 */
@Composable
fun AccountPickerDialog(
    accounts: List<Account>,
    selectedAccountId: Long,
    onAccountSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    showAllAccountsOption: Boolean = true
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Switch Account",
                color = DarkBrownText,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                // Only show "All Accounts" if requested AND there's more than 1 account
                if (showAllAccountsOption && accounts.size > 1) {
                    item {
                        AccountOptionRow(
                            name = "All Accounts",
                            color = Color.Gray,
                            isSelected = selectedAccountId == -1L,
                            onClick = { onAccountSelected(-1L); onDismiss() }
                        )
                    }
                }
                
                items(accounts) { account ->
                    AccountOptionRow(
                        name = account.name,
                        color = Color(account.color),
                        isSelected = selectedAccountId == account.id,
                        onClick = { onAccountSelected(account.id); onDismiss() }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = DarkBrownText)
            }
        },
        containerColor = BeigeHeader,
        shape = RoundedCornerShape(8.dp)
    )
}

/**
 * A single row representing an account option in the [AccountPickerDialog].
 */
@Composable
fun AccountOptionRow(
    name: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = DarkBrownText,
                unselectedColor = DarkBrownText.copy(alpha = 0.6f)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            color = DarkBrownText,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
