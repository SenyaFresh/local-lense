package ru.hse.edu.placemarks.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PlacemarkWithTags(
    @Embedded val placemark: PlacemarkDataEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlacemarkTagCrossRef::class,
            parentColumn = "placemark_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<TagDataEntity>
)