package ru.hse.edu.locallense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.hse.edu.geoar.heading.HeadingProvider
import ru.hse.edu.geoar.location.LocationTracker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Main {
                ArScreen(
                    activity = this,
                    locationTracker = LocationTracker(this),
                    headingProvider = HeadingProvider(this),
                )
            }
        }
    }
}