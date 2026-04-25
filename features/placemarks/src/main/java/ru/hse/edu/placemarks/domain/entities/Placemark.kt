package ru.hse.edu.placemarks.domain.entities

import androidx.compose.ui.graphics.Color
import ru.hse.locallense.common.entities.LocationData
import ru.hse.locallense.common.entities.Tag

data class Placemark(
    val id: Long,
    val color: Color,
    val name: String,
    val tags: List<Tag>,
    val locationData: LocationData,
    val type: Type,
) {
    sealed interface Type {
        data object Simple : Type
        data class Text(val text: String) : Type
        data class Photo(val filepath: String) : Type
        data class TextPhoto(val text: String, val photoPath: String) : Type
    }
}