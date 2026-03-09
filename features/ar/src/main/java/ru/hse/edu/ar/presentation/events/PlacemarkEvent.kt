package ru.hse.edu.ar.presentation.events

import ru.hse.edu.ar.domain.entities.ArPlacemark

sealed interface PlacemarkEvent {
    data class AddPlacemark(val placemark: ArPlacemark) : PlacemarkEvent

    data class DeletePlacemark(val id: Long) : PlacemarkEvent
}