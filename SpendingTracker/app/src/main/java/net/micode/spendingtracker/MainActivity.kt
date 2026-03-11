package net.micode.spendingtracker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationManagerCompat
import net.micode.spendingtracker.ui.screens.DashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkNotificationPermission()

        setContent {
            MaterialTheme {
                Surface {
                    DashboardScreen()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Volvemos a chequear al regresar de ajustes
        checkNotificationPermission(onlyShowIfDisabled = true)
    }

    private fun checkNotificationPermission(onlyShowIfDisabled: Boolean = false) {
        val packageName = packageName
        val listeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val isEnabled = listeners?.contains(packageName) == true

        if (isEnabled) {
            if (!onlyShowIfDisabled) {
                Toast.makeText(this, "Servicio de monitoreo activo ✅", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Por favor, activa el acceso a notificaciones para Spending Tracker ⚠️", Toast.LENGTH_LONG).show()
            // Redirigir a la configuración de Notification Listener
            try {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            } catch (e: Exception) {
                // Alternativa por si falla el intent directo
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    MaterialTheme {
        Surface {
            DashboardScreen()
        }
    }
}
