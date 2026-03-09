package ru.hse.edu.ar.domain.entities

import ru.hse.locallense.common.entities.LocationData

data class ArPlacemark(
    val id: Long,
    val name: String,
    val type: Type,
    val locationData: LocationData,
    val isWallAnchor: Boolean,
) {
    sealed interface Type {
        data object Simple : Type
        data class Text(val text: String) : Type
        data class Photo(val filepath: String) : Type
        data class Audio(val filepath: String) : Type
    }
}