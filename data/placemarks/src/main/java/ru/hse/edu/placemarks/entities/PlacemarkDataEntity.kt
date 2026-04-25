package ru.hse.edu.placemarks.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "placemarks")
data class PlacemarkDataEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val altitude: Double,
    @ColumnInfo(name = "is_wall_anchor") val isWallAnchor: Boolean,
    val color: Int,
    val type: PlacemarkType,
    val content: String?,
    @ColumnInfo(name = "content_secondary") val contentSecondary: String? = null,
)

enum class PlacemarkType {
    SIMPLE,
    TEXT,
    PHOTO,
    TEXT_PHOTO,
}