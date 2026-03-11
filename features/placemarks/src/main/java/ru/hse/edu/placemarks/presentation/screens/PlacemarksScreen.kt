package ru.hse.edu.placemarks.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.hse.edu.placemarks.di.PlacemarksDiContainer
import ru.hse.edu.placemarks.di.rememberPlacemarksDiContainer
import ru.hse.edu.placemarks.domain.entities.Placemark
import ru.hse.edu.placemarks.presentation.components.PlacemarksColumn
import ru.hse.edu.placemarks.presentation.events.PlacemarkEvent
import ru.hse.edu.placemarks.presentation.viewmodels.PlacemarksViewModel
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.common.entities.Tag

@Composable
fun PlacemarksScreen(
    diContainer: PlacemarksDiContainer = rememberPlacemarksDiContainer(),
    viewModel: PlacemarksViewModel = viewModel(factory = diContainer.viewModelFactory),
) {
    val placemarks by viewModel.placemarks.collectAsState()
    val tags by viewModel.tags.collectAsState()

    PlacemarksContent(
        placemarks = placemarks,
        tags = tags,
        onPlacemarkDelete = { viewModel.onEvent(PlacemarkEvent.DeletePlacemark(it.id)) },
    )
}

@Composable
fun PlacemarksContent(
    placemarks: ResultContainer<List<Placemark>>,
    tags: ResultContainer<List<Tag>>,
    onPlacemarkDelete: (Placemark) -> Unit,
) {
    PlacemarksColumn(
        placemarks = placemarks,
        onPlacemarkDelete = onPlacemarkDelete,
    )
}