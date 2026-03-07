package ru.hse.edu.locallense

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import kotlinx.coroutines.launch
import ru.hse.edu.geoar.ar.ArGeoEngine
import ru.hse.edu.geoar.ar.ArGeoObject
import ru.hse.edu.geoar.ar.ArGeoObjectPlacementResult
import ru.hse.edu.locallense.compose.extensions.createComposeViewNode

@Composable
fun ArScreen(
    activity: ComponentActivity,
) {
    var arGeoEngine by remember { mutableStateOf<ArGeoEngine?>(null) }
    var placementState by remember { mutableStateOf<ArGeoObjectPlacementResult?>(null) }

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
                    context = context,
                    scope = activity.lifecycleScope,
                )
                arGeoEngine = engine

                activity.lifecycleScope.launch {
                    val viewNode = sceneView.createComposeViewNode(activity) {
                        CounterButton()
                    }

                    engine.onTap = { location ->
                        if (location != null) {
                            val arGeoObject = ArGeoObject(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                altitude = location.altitude,
                                node = viewNode,
                                isWallAnchor = true,
                            )
                            engine.place(arGeoObject)
                        }
                    }

//                    val arGeoObject = ArGeoObject(
//                        latitude = 55.6064317,
//                        longitude = 37.41246,
//                        altitude = 200.0,
//                        node = viewNode,
//                        isWallAnchor = true,
//                    )
//                    engine.place(arGeoObject).collect {
//                        placementState = it
//                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
        onRelease = { sceneView ->
            arGeoEngine?.clear()
            sceneView.destroy()
        }
    )

    Text(placementState.toString())
}

@Composable
fun CounterButton() {
    var clickCount by remember { mutableIntStateOf(0) }

    Button(
        onClick = { clickCount++ },
        modifier = Modifier
            .width(300.dp)
            .height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200EE)
        )
    ) {
        Text(
            text = "Pressed: $clickCount",
            fontSize = 32.sp,
            color = Color.White
        )
    }
}