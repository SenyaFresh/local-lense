package ru.hse.edu.placemarks.repositories

import kotlinx.coroutines.flow.Flow
import ru.hse.edu.placemarks.entities.PlacemarkDataEntity
import ru.hse.edu.placemarks.entities.PlacemarkWithTags
import ru.hse.edu.placemarks.entities.TagDataEntity
import ru.hse.locallense.common.ResultContainer

interface PlacemarksDataRepository {

    suspend fun getPlacemarks(): Flow<ResultContainer<List<PlacemarkWithTags>>>

    suspend fun addPlacemark(placemark: PlacemarkWithTags)

    suspend fun deletePlacemark(id: Long)

    suspend fun getTags(): Flow<ResultContainer<List<TagDataEntity>>>

    suspend fun addTag(tag: TagDataEntity)

    suspend fun deleteTag(id: Long)
}