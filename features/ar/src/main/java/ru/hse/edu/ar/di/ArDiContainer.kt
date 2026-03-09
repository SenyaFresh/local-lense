package ru.hse.edu.ar.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import ru.hse.edu.ar.presentation.viewmodels.ArViewModel
import javax.inject.Inject

@Stable
class ArDiContainer {
    @Inject lateinit var viewModelFactory: ArViewModel.Factory
}

@Composable
fun rememberArDiContainer() : ArDiContainer {
    return remember {
        ArDiContainer().also {
            DaggerArComponent.builder().deps(ArDepsProvider.deps).build().inject(it)
        }
    }
}