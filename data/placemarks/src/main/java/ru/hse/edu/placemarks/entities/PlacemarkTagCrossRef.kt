package ru.hse.edu.placemarks.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "placemark_tag_cross_ref",
    primaryKeys = ["placemark_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = PlacemarkDataEntity::class,
            parentColumns = ["id"],
            childColumns = ["placemark_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagDataEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlacemarkTagCrossRef(
    @ColumnInfo(name = "placemark_id") val placemarkId: Long,
    @ColumnInfo(name = "tag_id") val tagId: Long,
)