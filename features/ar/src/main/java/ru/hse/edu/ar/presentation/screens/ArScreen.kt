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

sealed class ArScreenMode {
    data object ViewAll : ArScreenMode()
    data class ViewSingle(val placemarkId: Long) : ArScreenMode()
    data object AddNew : ArScreenMode()
}

@Composable
fun ArScreen(
    mode: ArScreenMode,
    diContainer: ArDiContainer = rememberArDiContainer(),
    viewModel: ArViewModel = viewModel(factory = diContainer.viewModelFactory),
) {
    var tapResult by remember { mutableStateOf<ArTapResult?>(null) }

    val markersResult by viewModel.placemarks.collectAsState()
    val tagsResult by viewModel.tags.collectAsState()

    if (mode is ArScreenMode.AddNew) {
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
    }

    when (mode) {
        is ArScreenMode.AddNew -> {
            ArContent(
                markers = emptyList(),
                mode = mode,
                onArTap = { tapResult = it },
            )
        }
        else -> {
            ResultContainerComposable(
                container = markersResult,
                onTryAgain = { },
                onSuccess = {
                    val markers = when (mode) {
                        is ArScreenMode.ViewAll -> markersResult.unwrap()
                        is ArScreenMode.ViewSingle -> markersResult.unwrap().filter { it.id == mode.placemarkId }
                    }
                    ArContent(
                        markers = markers,
                        mode = mode,
                        onArTap = null,
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
) {
    val coroutineScope = rememberCoroutineScope()
    val activity = LocalActivity.current as ComponentActivity
    var arGeoEngine by remember { mutableStateOf<ArGeoEngine?>(null) }

    val isPlacementMode = mode is ArScreenMode.AddNew

    LaunchedEffect(arGeoEngine, isPlacementMode) {
        if (arGeoEngine == null) return@LaunchedEffect
        if (isPlacementMode) {
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
                onArTap?.invoke(tapResult)
            }

            if (!isPlacementMode) {
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