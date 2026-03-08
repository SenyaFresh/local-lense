package ru.hse.edu.placemarks.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import ru.hse.edu.placemarks.presentation.viewmodels.PlacemarksViewModel
import javax.inject.Inject

@Stable
class PlacemarksDiContainer {
    @Inject lateinit var viewModelFactory: PlacemarksViewModel.Factory
}

@Composable
fun rememberPlacemarksDiContainer() : PlacemarksDiContainer {
    return remember {
        PlacemarksDiContainer().also {
            DaggerPlacemarksComponent.builder().deps(PlacemarksDepsProvider.deps).build().inject(it)
        }
    }
}