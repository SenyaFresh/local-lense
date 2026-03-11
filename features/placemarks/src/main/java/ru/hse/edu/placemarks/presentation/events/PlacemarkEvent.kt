package ru.hse.edu.placemarks.presentation.events

import ru.hse.locallense.common.entities.Tag

sealed interface PlacemarkEvent {
    data class DeletePlacemark(val id: Long) : PlacemarkEvent

    data class DeleteTag(val id: Long) : PlacemarkEvent

    data class AddTag(val tag: Tag) : PlacemarkEvent
}