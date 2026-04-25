package ru.hse.edu.ar.domain.entities

import androidx.compose.ui.graphics.Color
import ru.hse.locallense.common.entities.LocationData
import ru.hse.locallense.common.entities.Tag

data class ArPlacemark(
    val id: Long,
    val name: String,
    val type: Type,
    val color: Color,
    val tags: List<Tag>,
    val locationData: LocationData,
    val isWallAnchor: Boolean,
) {
    sealed interface Type {
        data object Simple : Type
        data class Text(val text: String) : Type
        data class Photo(val filepath: String) : Type
        data class TextPhoto(val text: String, val photoPath: String) : Type
    }
}