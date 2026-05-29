package net.micode.spendingtracker.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.R
import net.micode.spendingtracker.model.Account
import net.micode.spendingtracker.ui.components.AccountPickerDialog
import net.micode.spendingtracker.ui.components.CategorySelectionToolbar
import net.micode.spendingtracker.ui.components.ChalkDatePickerDialog
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.viewmodel.AccountViewModel
import java.text.SimpleDateFormat
import java.util.*

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
    
    var showTransferDialog by remember { mutableStateOf(false) }

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
                    onClick = { showTransferDialog = true },
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

    if (showTransferDialog) {
        TransferDialog(
            accounts = accounts,
            onDismiss = { showTransferDialog = false },
            onTransfer = { from, to, amount, date, note ->
                viewModel.transferFunds(from, to, amount, date, note)
                showTransferDialog = false
            }
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

@Composable
fun TransferDialog(
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onTransfer: (from: Long, to: Long, amount: Double, date: Long, note: String) -> Unit
) {
    var fromAccount by remember { mutableStateOf(accounts.firstOrNull()) }
    var toAccount by remember { mutableStateOf(accounts.getOrNull(1)) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val isFormValid = fromAccount != null && toAccount != null && fromAccount?.id != toAccount?.id && amount.toDoubleOrNull() != null && amount.toDouble() > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transfer Funds", color = DarkBrownText, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("From", color = Color(0xFF1976D2), fontSize = 12.sp)
                Box(modifier = Modifier.fillMaxWidth().clickable { showFromPicker = true }.padding(vertical = 8.dp)) {
                    Text(fromAccount?.name ?: "Select Account", color = DarkBrownText, fontSize = 16.sp)
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                Spacer(modifier = Modifier.height(16.dp))

                Text("To", color = Color(0xFF1976D2), fontSize = 12.sp)
                Box(modifier = Modifier.fillMaxWidth().clickable { showToPicker = true }.padding(vertical = 8.dp)) {
                    Text(toAccount?.name ?: "Select Account", color = DarkBrownText, fontSize = 16.sp)
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Amount", color = Color(0xFF1976D2), fontSize = 12.sp)
                BasicTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = TextStyle(fontSize = 16.sp, color = DarkBrownText),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    decorationBox = { innerTextField ->
                        if (amount.isEmpty()) Text("0.00", color = Color.Gray.copy(alpha = 0.5f), fontSize = 16.sp)
                        innerTextField()
                    }
                )
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Date", color = Color(0xFF1976D2), fontSize = 12.sp)
                Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }.padding(vertical = 8.dp)) {
                    Text(dateFormatter.format(Date(dateMillis)), color = DarkBrownText, fontSize = 16.sp)
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Note", color = Color(0xFF1976D2), fontSize = 12.sp)
                BasicTextField(
                    value = note,
                    onValueChange = { note = it },
                    textStyle = TextStyle(fontSize = 16.sp, color = DarkBrownText),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    decorationBox = { innerTextField ->
                        if (note.isEmpty()) Text("Optional", color = Color.Gray.copy(alpha = 0.5f), fontSize = 16.sp)
                        innerTextField()
                    }
                )
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            }
        },
        confirmButton = {
            TextButton(
                enabled = isFormValid,
                onClick = {
                    onTransfer(fromAccount!!.id, toAccount!!.id, amount.toDouble(), dateMillis, note)
                }
            ) {
                Text("TRANSFER", color = if (isFormValid) Color(0xFF4CAF50) else Color.Gray, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = DarkBrownText)
            }
        },
        containerColor = BeigeHeader,
        shape = RoundedCornerShape(8.dp)
    )

    if (showFromPicker) {
        AccountPickerDialog(
            accounts = accounts,
            selectedAccountId = fromAccount?.id ?: -1L,
            onAccountSelected = { id -> fromAccount = accounts.find { it.id == id }; showFromPicker = false },
            onDismiss = { showFromPicker = false },
            showAllAccountsOption = false
        )
    }

    if (showToPicker) {
        AccountPickerDialog(
            accounts = accounts.filter { it.id != fromAccount?.id },
            selectedAccountId = toAccount?.id ?: -1L,
            onAccountSelected = { id -> toAccount = accounts.find { it.id == id }; showToPicker = false },
            onDismiss = { showToPicker = false },
            showAllAccountsOption = false
        )
    }

    if (showDatePicker) {
        ChalkDatePickerDialog(
            initialDateMillis = dateMillis,
            onDateSelected = { dateMillis = it; showDatePicker = false },
            onDismiss = { showDatePicker = false }
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
