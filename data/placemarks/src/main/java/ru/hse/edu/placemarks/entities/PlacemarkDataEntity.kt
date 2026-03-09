package ru.hse.edu.placemarks.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "placemarks",
)
data class PlacemarkDataEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val altitude: Double,
    val type: PlacemarkType,
    val content: String?,
)

enum class PlacemarkType {
    SIMPLE,
    TEXT,
    PHOTO,
    AUDIO,
}