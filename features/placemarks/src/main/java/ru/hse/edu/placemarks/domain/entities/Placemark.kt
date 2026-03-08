package ru.hse.edu.placemarks.domain.entities

data class Placemark(
    val id: Long,
    val name: String,
    val type: Type,
    val longitude: Double,
    val latitude: Double,
    val altitude: Double,
) {
    sealed interface Type {
        data object Simple : Type
        data class Text(val text: String) : Type
        data class Photo(val filepath: String) : Type
        data class Audio(val filepath: String) : Type
    }
}