package ru.hse.edu.ar.presentation.events

import ru.hse.edu.ar.domain.entities.ArPlacemark
import ru.hse.locallense.common.entities.Tag

sealed interface PlacemarkEvent {
    data class AddPlacemark(val placemark: ArPlacemark) : PlacemarkEvent

    data class DeletePlacemark(val id: Long) : PlacemarkEvent

    data class AddTag(val tag: Tag) : PlacemarkEvent
    data class DeleteTag(val id: Long) : PlacemarkEvent
}