package ru.hse.edu.placemarks.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.hse.locallense.presentation.BaseViewModel
import javax.inject.Inject
import kotlin.jvm.java

class PlacemarksViewModel @Inject constructor(
): BaseViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == PlacemarksViewModel::class.java)
            return PlacemarksViewModel() as T
        }
    }
}