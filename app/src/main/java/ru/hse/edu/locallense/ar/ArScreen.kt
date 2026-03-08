package ru.hse.edu.locallense.ar

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.sceneview.ar.ARSceneView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.hse.edu.geoar.ar.ArGeoEngine
import ru.hse.edu.geoar.ar.ArGeoObject
import ru.hse.edu.geoar.ar.ArGeoObjectPlacementResult
import ru.hse.edu.geoar.ar.ArTapResult
import ru.hse.locallense.common.entities.LocationData
import ru.hse.locallense.components.composables.sceneview.createComposeViewNode

@Composable
fun ArScreen() {
    val markers = remember {
        mutableStateListOf(
            ArGeoMarker(
                locationData = LocationData(
                    latitude = 55.6064317,
                    longitude = 37.41246,
                    altitude = 200.0,
                ),
                isWallAnchor = true,
            ),
            ArGeoMarker(
                locationData = LocationData(
                    latitude = 55.6024317,
                    longitude = 37.41046,
                    altitude = 200.0,
                ),
                isWallAnchor = true,
            ),
        )
    }
    ArContent(
        markers = markers,
        onNewArGeoMarker = { marker ->
            markers.add(marker)
        }
    )
}

@Composable
fun ArContent(
    markers: List<ArGeoMarker>,
    onNewArGeoMarker: (ArGeoMarker) -> Unit,
) {
    ArSceneViewComposable(
        markers = markers,
        onArTap = { tapResult ->
            if (tapResult == null) {
                return@ArSceneViewComposable
            }
            onNewArGeoMarker(
                ArGeoMarker(
                    locationData = tapResult.locationData,
                    isWallAnchor = tapResult.isWall
                )
            )
        }
    )
}

@Composable
fun ArSceneViewComposable(
    markers: List<ArGeoMarker>,
    onArTap: (ArTapResult?) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val activity = LocalActivity.current as ComponentActivity
    var arGeoEngine by remember { mutableStateOf<ArGeoEngine?>(null) }

    AndroidView(
        factory = { context ->
            ARSceneView(context).also { sceneView ->
                arGeoEngine = ArGeoEngine(
                    sceneView = sceneView,
                    context = context,
                    scope = coroutineScope,
                )
            }
        },
        update = { sceneView ->
            val engine = arGeoEngine ?: return@AndroidView
            engine.onTap = { tapResult -> onArTap(tapResult) }

            engine.clear()
            markers.forEach { marker ->
                placeMarker(sceneView, activity, coroutineScope, engine, marker)
            }
        },
        modifier = Modifier.fillMaxSize(),
    )
}

private fun placeMarker(
    sceneView: ARSceneView,
    activity: ComponentActivity,
    coroutineScope: CoroutineScope,
    arGeoEngine: ArGeoEngine,
    marker: ArGeoMarker,
) {
    val placementResult = mutableStateOf<ArGeoObjectPlacementResult?>(null)

    val viewNode = sceneView.createComposeViewNode(activity) {
        ArGeoMarkerComposable(placementResult.value)
    }

    val arGeoObject = ArGeoObject(
        locationData = marker.locationData,
        node = viewNode,
        isWallAnchor = marker.isWallAnchor,
    )

    coroutineScope.launch {
        arGeoEngine.place(arGeoObject).collect { result ->
            placementResult.value = result
        }
    }
}