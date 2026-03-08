package ru.hse.edu.locallense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.hse.edu.locallense.navigation.AppNavigation
import ru.hse.edu.locallense.ui.theme.LocalLenseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalLenseTheme {
                AppNavigation()
            }
        }
    }
}