package ru.hse.edu.ar.domain.repositories

import kotlinx.coroutines.flow.Flow
import ru.hse.edu.ar.domain.entities.ArPlacemark
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.common.entities.Tag

interface ArPlacemarksRepository {

    suspend fun getPlacemarks(): Flow<ResultContainer<List<ArPlacemark>>>

    suspend fun addPlacemark(placemark: ArPlacemark)

    suspend fun deletePlacemark(id: Long)

    suspend fun getTags(): Flow<ResultContainer<List<Tag>>>

    suspend fun addTag(tag: Tag)

    suspend fun deleteTag(id: Long)
}