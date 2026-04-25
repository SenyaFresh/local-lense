package ru.hse.edu.locallense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import ru.hse.edu.geoar.ar.ArGeoFactory
import ru.hse.edu.locallense.navigation.AppNavigation
import ru.hse.edu.locallense.ui.theme.LocalLenseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ArGeoFactory.init(this, this.lifecycleScope)
        enableEdgeToEdge()
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                ArGeoFactory.activeArPoseLocationTracker?.persistSnapshotNow()
            }
        })
        setContent {
            LocalLenseTheme {
                AppNavigation()
            }
        }
    }
}