package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.R
import net.micode.spendingtracker.model.Account
import net.micode.spendingtracker.ui.components.CategorySelectionToolbar
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.viewmodel.AccountViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountsScreen(
    viewModel: AccountViewModel,
    selectedAccountId: Long,
    onEditAccount: (Account) -> Unit
) {
    val accounts by viewModel.allAccounts.collectAsState()
    var selectedAccountIds by remember { mutableStateOf(setOf<Long>()) }
    
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteErrorDialog by remember { mutableStateOf(false) }
    var accountErrorName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeHeader)
    ) {
        // Multi-selection Toolbar
        if (selectedAccountIds.isNotEmpty()) {
            CategorySelectionToolbar(
                selectedCount = selectedAccountIds.size,
                onClearSelection = { selectedAccountIds = emptySet() },
                onEdit = {
                    val account = accounts.find { it.id == selectedAccountIds.first() }
                    account?.let { onEditAccount(it) }
                    selectedAccountIds = emptySet()
                },
                onDelete = {
                    if (selectedAccountIds.contains(1L)) {
                        accountErrorName = accounts.find { it.id == 1L }?.name ?: "Default"
                        showDeleteErrorDialog = true
                    } else {
                        showDeleteConfirmDialog = true
                    }
                }
            )
        }

        // Transfer Button (Only visible if > 1 account)
        if (accounts.size > 1) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { /* Transfer Logic */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(48.dp).width(120.dp)
                ) {
                    Text("Transfer", color = Color.White, fontSize = 18.sp)
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(accounts) { account ->
                val isSelected = selectedAccountIds.contains(account.id)
                val isProtected = account.id == 1L
                val isCurrentlyActive = account.id == selectedAccountId

                AccountItem(
                    account = account,
                    isSelected = isSelected,
                    isProtected = isProtected,
                    isCurrentlyActive = isCurrentlyActive,
                    onClick = {
                        if (selectedAccountIds.isEmpty()) {
                            // Restoration: Click to edit
                            onEditAccount(account)
                        } else {
                            selectedAccountIds = if (isSelected) selectedAccountIds - account.id else selectedAccountIds + account.id
                        }
                    },
                    onLongClick = {
                        selectedAccountIds = selectedAccountIds + account.id
                    }
                )
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
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

    if (showDeleteErrorDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteErrorDialog = false },
            title = { Text("Error", fontWeight = FontWeight.Bold, color = DarkBrownText) },
            text = { 
                Text(
                    text = "'$accountErrorName' is the 'Default' Account. It cannot be deleted.",
                    color = DarkBrownText
                ) 
            },
            confirmButton = {
                TextButton(onClick = { showDeleteErrorDialog = false }) {
                    Text("CLOSE", color = DarkBrownText, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(4.dp)
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(stringResource(R.string.delete_account), fontWeight = FontWeight.Bold, color = DarkBrownText) },
            text = { Text(stringResource(R.string.delete_confirm_msg), color = DarkBrownText) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = accounts.filter { it.id in selectedAccountIds }
                        viewModel.deleteAccounts(toDelete)
                        selectedAccountIds = emptySet()
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete), color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel), color = DarkBrownText)
                }
            },
            containerColor = BeigeHeader,
            shape = RoundedCornerShape(4.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountItem(
    account: Account,
    isSelected: Boolean,
    isProtected: Boolean,
    isCurrentlyActive: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Color(0xFFE0F7FA) else Color.White)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.AccountCircle,
            contentDescription = null,
            tint = Color(account.color),
            modifier = Modifier.size(36.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = account.name,
            fontSize = 20.sp,
            color = DarkBrownText,
            modifier = Modifier.weight(1f)
        )
        if (isProtected) {
            Icon(
                Icons.Default.Lock,
                contentDescription = "Protected",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp).padding(end = 8.dp)
            )
        }
        if (isCurrentlyActive) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Active Account",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
