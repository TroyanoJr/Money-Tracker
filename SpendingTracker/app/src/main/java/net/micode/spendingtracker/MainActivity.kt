package net.micode.spendingtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            androidx.compose.material3.Surface(
                modifier = androidx.compose.ui.Modifier.fillMaxSize()
            ) {
                Text(text = "Hola Mundo")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)@Composable
fun GreetingPreview() {
    Surface(
        modifier = androidx.compose.ui.Modifier.fillMaxSize()
    ) {
        Text(text = "Hola Mundo")
    }
}