package ru.hse.edu.locallense

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import io.github.sceneview.ar.ARSceneView
import ru.hse.edu.geoar.ArGeoEngine
import ru.hse.edu.geoar.geo.GeoObject
import ru.hse.edu.geoar.geo.GeoUtils
import ru.hse.edu.geoar.heading.HeadingProvider
import ru.hse.edu.geoar.location.LocationTracker

@Composable
fun ArScreen(
    activity: ComponentActivity,
    locationTracker: LocationTracker,
    headingProvider: HeadingProvider
) {
    var arGeoEngine by remember { mutableStateOf<ArGeoEngine?>(null) }
    var geoObject by remember { mutableStateOf<GeoObject?>(null) }

    val location by locationTracker.locationState.collectAsState()
    val heading by headingProvider.heading.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            arGeoEngine?.clear()
        }
    }

    AndroidView(
        factory = { context ->
            ARSceneView(context).also { sceneView ->
                val engine = ArGeoEngine(
                    sceneView = sceneView,
                    locationTracker = locationTracker,
                    headingProvider = headingProvider,
                    scope = activity.lifecycleScope
                )
                arGeoEngine = engine

                val viewNode = sceneView.createComposeViewNode(activity) {
                    CounterButton()
                }

                geoObject = GeoObject(
                    latitude = 55.6068317,
                    longitude = 37.41446,
                    node = viewNode
                )

                engine.place(geoObject!!)
            }
        },
        modifier = Modifier.fillMaxSize(),
        onRelease = { sceneView ->
            arGeoEngine?.clear()
            sceneView.destroy()
        }
    )
    Column {
        Text("Current location: latitude=${location.unwrapOrNull()?.latitude}, longitude=${location.unwrapOrNull()?.longitude}")
        Text("Target location: latitude=${geoObject?.latitude}, longitude=${geoObject?.longitude}")
        location.unwrapOrNull()?.let {
            Text("Distance to target: ${GeoUtils.distanceMeters(it, geoObject!!)}")
            Text("Bearing to target: ${GeoUtils.relativeBearing(heading,it, geoObject!!)}")
        }
    }
}

@Composable
fun CounterButton() {
    var count by remember { mutableIntStateOf(0) }

    Button(
        onClick = { count++ },
        modifier = Modifier
            .width(300.dp)
            .height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200EE)
        )
    ) {
        Text(
            text = "Pressed: $count",
            fontSize = 32.sp,
            color = Color.White
        )
    }
}