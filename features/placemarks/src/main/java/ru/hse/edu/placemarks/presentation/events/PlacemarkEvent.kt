package ru.hse.edu.placemarks.presentation.events

import ru.hse.edu.placemarks.presentation.entities.SortType
import ru.hse.locallense.common.entities.Tag

sealed interface PlacemarkEvent {
    data class DeletePlacemark(val id: Long) : PlacemarkEvent
    data class DeleteTag(val id: Long) : PlacemarkEvent
    data class AddTag(val tag: Tag) : PlacemarkEvent
    data class SortBy(val sortType: SortType) : PlacemarkEvent
    data class SelectTag(val id: Long) : PlacemarkEvent
    data class SearchByName(val query: String) : PlacemarkEvent
}