package net.micode.moneytracker.ui.screens

import android.content.Intent
import android.os.Process
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.moneytracker.SpendingTrackerApp
import net.micode.moneytracker.ui.theme.BeigeHeader
import net.micode.moneytracker.ui.theme.DarkBrownText
import net.micode.moneytracker.util.BackupManager
import java.text.SimpleDateFormat
import java.util.*
import net.micode.moneytracker.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as SpendingTrackerApp
    var showLocalOptions by remember { mutableStateOf(false) }

    // Launcher for creating a new backup ZIP file
    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let {
            val success = BackupManager.createBackup(context, app.database, it)
            if (success) {
                Toast.makeText(context, context.getString(R.string.backup_success), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, context.getString(R.string.backup_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher for selecting an existing backup ZIP file to restore
    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            // Close DB before restoring
            app.database.close()
            
            val success = BackupManager.restoreBackup(context, it)
            if (success) {
                Toast.makeText(context, context.getString(R.string.restore_success), Toast.LENGTH_LONG).show()
                
                // Full process restart to clear cache and reload DB with new restored key
                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(intent)
                
                Process.killProcess(Process.myPid())
            } else {
                Toast.makeText(context, context.getString(R.string.restore_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.backups), color = DarkBrownText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkBrownText)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Future options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = DarkBrownText)
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!showLocalOptions) {
                // Main Button following reference design
                Button(
                    onClick = { showLocalOptions = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.local_backup).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Text(
                    text = stringResource(R.string.backup_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.local_backup),
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkBrownText,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                OutlinedButton(
                    onClick = {
                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        createBackupLauncher.launch("SpendingTracker_Backup_$timeStamp.zip")
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(stringResource(R.string.create_new_backup))
                }

                OutlinedButton(
                    onClick = {
                        restoreBackupLauncher.launch(arrayOf("application/zip"))
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(stringResource(R.string.restore_existing_backup))
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                TextButton(onClick = { showLocalOptions = false }) {
                    Text("Back to selection", color = DarkBrownText)
                }
            }
        }
    }
}
