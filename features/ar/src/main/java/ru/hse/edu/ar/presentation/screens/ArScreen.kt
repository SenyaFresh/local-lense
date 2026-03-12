package ru.hse.edu.ar.presentation.screens

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.sceneview.ar.ARSceneView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.hse.edu.ar.di.ArDiContainer
import ru.hse.edu.ar.di.rememberArDiContainer
import ru.hse.edu.ar.domain.entities.ArPlacemark
import ru.hse.edu.ar.presentation.components.AddPlacemarkDialog
import ru.hse.edu.ar.presentation.components.ArGeoMarkerComposable
import ru.hse.edu.ar.presentation.events.PlacemarkEvent
import ru.hse.edu.ar.presentation.viewmodels.ArViewModel
import ru.hse.edu.geoar.ar.ArGeoEngine
import ru.hse.edu.geoar.ar.ArGeoEngineMode
import ru.hse.edu.geoar.ar.ArGeoObject
import ru.hse.edu.geoar.ar.ArGeoObjectPlacementResult
import ru.hse.edu.geoar.ar.ArTapResult
import ru.hse.locallense.common.entities.LocationData
import ru.hse.locallense.components.composables.sceneview.createComposeViewNode
import ru.hse.locallense.presentation.ResultContainerComposable

@Composable
fun ArScreen(
    diContainer: ArDiContainer = rememberArDiContainer(),
    viewModel: ArViewModel = viewModel(factory = diContainer.viewModelFactory),
    isNewMarkMode: Boolean = false,
) {
    var tapResult by remember { mutableStateOf<ArTapResult?>(null) }

    val hardMarkers = remember {
        mutableStateListOf(
            ArPlacemark(
                id = 0,
                name = "Метка",
                type = ArPlacemark.Type.Simple,
                locationData = LocationData(
                    latitude = 55.6064317,
                    longitude = 37.41246,
                    altitude = 200.0,
                ),
                color = Color(0xFF7C4DFF),
                tags = emptyList(),
                isWallAnchor = true,
            ),
            ArPlacemark(
                id = 1,
                name = "Текстовая метка",
                type = ArPlacemark.Type.Text("Текст какой-то метки"),
                locationData = LocationData(
                    latitude = 55.6068951,
                    longitude = 37.4144355,
                    altitude = 200.0,
                ),
                color = Color(0xFF7C4D00),
                tags = emptyList(),
                isWallAnchor = false,
            ),
            ArPlacemark(
                id = 2,
                name = "Другая тестовая метка",
                type = ArPlacemark.Type.Text("Другой текст другой метки"),
                locationData = LocationData(
                    latitude = 55.6066951,
                    longitude = 37.4141355,
                    altitude = 210.0,
                ),
                color = Color(0xFF7C4DFF),
                tags = emptyList(),
                isWallAnchor = true,
            ),
        )
    }

    val markersResult by viewModel.placemarks.collectAsState()
    val tagsResult by viewModel.tags.collectAsState()


    tapResult?.let { result ->
        AddPlacemarkDialog(
            tapResult = result,
            onDismiss = { tapResult = null },
            onConfirm = { placemark ->
                viewModel.onEvent(PlacemarkEvent.AddPlacemark(placemark))
                tapResult = null
            },
            onAddTag = { tag ->
                viewModel.onEvent(PlacemarkEvent.AddTag(tag))
            },
            onDeleteTag = { id ->
                viewModel.onEvent(PlacemarkEvent.DeleteTag(id))
            },
            availableTags = tagsResult.unwrapOrNull() ?: emptyList(),
        )
    }

    ResultContainerComposable(
        container = markersResult,
        onTryAgain = { },
        onSuccess = {
            ArContent(
                markers = markersResult.unwrap() + hardMarkers,
                onArTap = { result ->
                    tapResult = result
                },
                isNewMarkMode = isNewMarkMode,
            )
        }
    )
}

@Composable
fun ArContent(
    markers: List<ArPlacemark>,
    onArTap: (ArTapResult?) -> Unit,
    isNewMarkMode: Boolean,
) {
    val coroutineScope = rememberCoroutineScope()
    val activity = LocalActivity.current as ComponentActivity
    var arGeoEngine by remember { mutableStateOf<ArGeoEngine?>(null) }

    LaunchedEffect(arGeoEngine, isNewMarkMode) {
        if (arGeoEngine == null) return@LaunchedEffect
        if (isNewMarkMode) {
            arGeoEngine?.mode = ArGeoEngineMode.PLACEMENT
            arGeoEngine?.clear()
        } else {
            arGeoEngine?.mode = ArGeoEngineMode.VIEW
        }
    }

    AndroidView(
        factory = { context ->
            ARSceneView(context).also { sceneView ->
                arGeoEngine = ArGeoEngine(
                    sceneView = sceneView,
                    scope = coroutineScope,
                )
            }
        },
        update = { sceneView ->
            val engine = arGeoEngine ?: return@AndroidView
            engine.onTap = { tapResult ->
                onArTap(tapResult)
            }

            if (!isNewMarkMode) {
                markers.forEach { marker ->
                    placeMarker(sceneView, activity, coroutineScope, engine, marker)
                }
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
    marker: ArPlacemark,
) {
    val placementResult = mutableStateOf<ArGeoObjectPlacementResult?>(null)

    val viewNode = sceneView.createComposeViewNode(activity) {
        ArGeoMarkerComposable(marker, placementResult.value)
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