package ru.hse.edu.placemarks.presentation.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.hse.edu.placemarks.di.PlacemarksDiContainer
import ru.hse.edu.placemarks.di.rememberPlacemarksDiContainer
import ru.hse.edu.placemarks.presentation.viewmodels.PlacemarksViewModel

@Composable
fun PlacemarksScreen(
    diContainer: PlacemarksDiContainer = rememberPlacemarksDiContainer(),
    viewModel: PlacemarksViewModel = viewModel(factory = diContainer.viewModelFactory),
) {
    PlacemarksContent()
}

@Composable
fun PlacemarksContent() {

}