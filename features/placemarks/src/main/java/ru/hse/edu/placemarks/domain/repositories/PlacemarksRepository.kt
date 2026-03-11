package ru.hse.edu.placemarks.domain.repositories

import kotlinx.coroutines.flow.Flow
import ru.hse.edu.placemarks.domain.entities.Placemark
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.common.entities.Tag

interface PlacemarksRepository {
    suspend fun getPlacemarks(): Flow<ResultContainer<List<Placemark>>>

    suspend fun deletePlacemark(id: Long)

    suspend fun getTags(): Flow<ResultContainer<List<Tag>>>

    suspend fun addTag(tag: Tag)

    suspend fun deleteTag(id: Long)
}