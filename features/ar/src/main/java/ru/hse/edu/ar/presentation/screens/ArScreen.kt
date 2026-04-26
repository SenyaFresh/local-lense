package ru.hse.edu.ar.presentation.screens

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.sceneview.ar.ARSceneView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.hse.edu.ar.di.ArDiContainer
import ru.hse.edu.ar.di.rememberArDiContainer
import ru.hse.edu.ar.domain.entities.ArPlacemark
import ru.hse.edu.ar.presentation.components.AddPlacemarkDialog
import ru.hse.edu.ar.presentation.components.ArGeoMarker
import ru.hse.edu.ar.presentation.events.PlacemarkEvent
import ru.hse.edu.ar.presentation.viewmodels.ArViewModel
import ru.hse.edu.geoar.ar.ArGeoEngine
import ru.hse.edu.geoar.ar.ArGeoEngineMode
import ru.hse.edu.geoar.ar.ArGeoObject
import ru.hse.edu.geoar.ar.ArGeoObjectPlacementResult
import ru.hse.edu.geoar.ar.ArTapResult
import ru.hse.locallense.components.composables.sceneview.createComposeViewNode
import ru.hse.locallense.presentation.ResultContent

sealed class ArScreenMode {
    data object ViewAll : ArScreenMode()
    data class ViewSingle(val placemarkId: Long) : ArScreenMode()
    data object AddNew : ArScreenMode()
}

@Composable
fun ArScreen(
    mode: ArScreenMode,
    arGeoEngine: ArGeoEngine,
    arSceneView: ARSceneView,
    onPlacemarkAdded: () -> Unit,
    onCompassMarkersChange: (List<ArPlacemark>) -> Unit = {},
    diContainer: ArDiContainer = rememberArDiContainer(),
    viewModel: ArViewModel = viewModel(factory = diContainer.viewModelFactory),
) {
    var tapResult by remember { mutableStateOf<ArTapResult?>(null) }

    val markersResult by viewModel.placemarks.collectAsState()
    val tagsResult by viewModel.tags.collectAsState()

    if (mode is ArScreenMode.AddNew) {
        tapResult?.let { result ->
            AddPlacemarkDialog(
                locationData = result.locationData,
                isWallAnchor = result.isWall,
                onDismiss = { tapResult = null },
                onConfirm = { placemark ->
                    viewModel.onEvent(PlacemarkEvent.AddPlacemark(placemark))
                    tapResult = null
                    onPlacemarkAdded()
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
    }

    when (mode) {
        is ArScreenMode.AddNew -> {
            ArContent(
                markers = emptyList(),
                mode = mode,
                onArTap = { tapResult = it },
                arGeoEngine = arGeoEngine,
                arSceneView = arSceneView,
                onCompassMarkersChange = onCompassMarkersChange,
            )
        }

        else -> {
            ResultContent(
                container = markersResult,
                onTryAgain = { },
                onSuccess = {
                    val markers = when (mode) {
                        is ArScreenMode.ViewAll -> markersResult.unwrap()
                        is ArScreenMode.ViewSingle -> markersResult.unwrap()
                            .filter { it.id == mode.placemarkId }

                        else -> emptyList()
                    }
                    ArContent(
                        markers = markers,
                        mode = mode,
                        onArTap = null,
                        arGeoEngine = arGeoEngine,
                        arSceneView = arSceneView,
                        onCompassMarkersChange = onCompassMarkersChange,
                    )
                }
            )
        }
    }
}

@Composable
fun ArContent(
    markers: List<ArPlacemark>,
    mode: ArScreenMode,
    onArTap: ((ArTapResult?) -> Unit)?,
    arGeoEngine: ArGeoEngine,
    arSceneView: ARSceneView,
    onCompassMarkersChange: (List<ArPlacemark>) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val activity = LocalActivity.current as ComponentActivity

    val isPlacementMode = mode is ArScreenMode.AddNew

    LaunchedEffect(arGeoEngine, isPlacementMode) {
        if (isPlacementMode) {
            arGeoEngine.mode = ArGeoEngineMode.PLACEMENT
            arGeoEngine.clear()
        } else {
            arGeoEngine.mode = ArGeoEngineMode.VIEW
        }
    }

    SideEffect {
        arGeoEngine.onTap = { tapResult ->
            onArTap?.invoke(tapResult)
        }
    }

    if (!isPlacementMode) {
        LaunchedEffect(markers) {
            markers.forEach { marker ->
                placeMarker(arSceneView, activity, coroutineScope, arGeoEngine, marker)
            }
        }
    }

    LaunchedEffect(markers, isPlacementMode) {
        onCompassMarkersChange(if (isPlacementMode) emptyList() else markers)
    }

    DisposableEffect(Unit) {
        onDispose {
            arGeoEngine.onTap = null
            arGeoEngine.clear()
            onCompassMarkersChange(emptyList())
        }
    }
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
        ArGeoMarker(marker, placementResult.value)
    }

    val arGeoObject = ArGeoObject(
        id = marker.id,
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