package ru.hse.edu.ar.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.hse.edu.ar.R
import ru.hse.edu.ar.di.ArDiContainer
import ru.hse.edu.ar.di.rememberArDiContainer
import ru.hse.edu.ar.presentation.components.AddPlacemarkDialog
import ru.hse.edu.ar.presentation.events.PlacemarkEvent
import ru.hse.edu.ar.presentation.mapkit.LocationPicker
import ru.hse.edu.ar.presentation.mapkit.PlacemarksMap
import ru.hse.edu.ar.presentation.viewmodels.ArViewModel
import ru.hse.edu.geoar.ar.ArGeoFactory
import ru.hse.locallense.common.entities.LocationData
import ru.hse.locallense.presentation.ResultContent

sealed class MapScreenMode {
    data object ViewAll : MapScreenMode()
    data class ViewSingle(val placemarkId: Long) : MapScreenMode()
    data object AddNew : MapScreenMode()
}

@Composable
fun MapScreen(
    mode: MapScreenMode,
    initialLatitude: Double,
    initialLongitude: Double,
    onPlacemarkAdded: () -> Unit,
    diContainer: ArDiContainer = rememberArDiContainer(),
    viewModel: ArViewModel = viewModel(factory = diContainer.viewModelFactory),
) {
    var pickedLocation by remember { mutableStateOf<LocationData?>(null) }

    val markersResult by viewModel.placemarks.collectAsState()
    val tagsResult by viewModel.tags.collectAsState()

    if (mode is MapScreenMode.AddNew) {
        pickedLocation?.let { location ->
            AddPlacemarkDialog(
                locationData = location,
                onDismiss = { pickedLocation = null },
                onConfirm = { placemark ->
                    viewModel.onEvent(PlacemarkEvent.AddPlacemark(placemark))
                    pickedLocation = null
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
        is MapScreenMode.AddNew -> {
            LocationPicker(
                title = stringResource(R.string.ar_map_pick_location_title),
                initialLatitude = initialLatitude,
                initialLongitude = initialLongitude,
                onConfirm = { lat, lng ->
                    pickedLocation = LocationData(
                        lat,
                        lng,
                        ArGeoFactory.locationTracker.locationState.value?.altitude ?: 200.0
                    )
                },
            )
        }

        else -> {
            ResultContent(
                container = markersResult,
                onTryAgain = { },
                onSuccess = {
                    val markers = when (mode) {
                        is MapScreenMode.ViewAll -> markersResult.unwrap()
                        is MapScreenMode.ViewSingle -> markersResult.unwrap()
                            .filter { it.id == mode.placemarkId }
                    }
                    PlacemarksMap(
                        placemarks = markers,
                        initialLatitude = initialLatitude,
                        initialLongitude = initialLongitude,
                        onPlacemarkClick = { },
                    )
                }
            )
        }
    }
}