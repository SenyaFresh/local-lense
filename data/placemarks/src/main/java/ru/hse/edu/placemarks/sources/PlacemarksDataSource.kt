package ru.hse.edu.placemarks.sources

import ru.hse.edu.placemarks.entities.PlacemarkDataEntity
import ru.hse.edu.placemarks.entities.PlacemarkWithTags
import ru.hse.edu.placemarks.entities.TagDataEntity

interface PlacemarksDataSource {

    suspend fun getPlacemarks(): List<PlacemarkWithTags>

    suspend fun addPlacemark(placemark: PlacemarkWithTags)

    suspend fun deletePlacemark(id: Long)

    suspend fun getTags(): List<TagDataEntity>

    suspend fun addTag(tag: TagDataEntity)

    suspend fun deleteTag(id: Long)
}