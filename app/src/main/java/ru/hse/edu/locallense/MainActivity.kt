package ru.hse.edu.locallense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import ru.hse.edu.geoar.location.LocationTracker
import ru.hse.edu.locallense.ui.theme.LocalLenseTheme
import ru.hse.locallense.presentation.ResultContainerComposable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val location = LocationTracker(lifecycleScope, this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Main {
                val state by location.locationState.collectAsState()
                ResultContainerComposable(
                    container = state,
                    onTryAgain = {},
                    onSuccess = {
                        Text("Altitude: ${state.unwrapOrNull()?.altitude}; Longitude: ${state.unwrapOrNull()?.longitude}")
                    }
                )
            }
        }
    }
}

@Composable
fun Main(content: @Composable () -> Unit) {
    LocalLenseTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier.padding(innerPadding)
            ) {
                content()
            }
        }
    }
}