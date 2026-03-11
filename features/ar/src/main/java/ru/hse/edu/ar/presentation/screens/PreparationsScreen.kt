package ru.hse.edu.ar.presentation.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yandex.mapkit.mapview.MapView
import ru.hse.edu.ar.di.ArDiContainer
import ru.hse.edu.ar.di.rememberArDiContainer
import ru.hse.edu.ar.presentation.viewmodels.ArViewModel

@Composable
fun PreparationsScreen(
    diContainer: ArDiContainer = rememberArDiContainer(),
    viewModel: ArViewModel = viewModel(factory = diContainer.viewModelFactory),
) {

}

@Composable
fun PreparationsContent() {

}