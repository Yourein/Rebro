package net.yourein.rebro.core.application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import net.yourein.rebro.core.navigation.RebroNavDisplay
import net.yourein.rebro.core.resources.RebroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RebroTheme {
                RebroNavDisplay()
            }
        }
    }
}
