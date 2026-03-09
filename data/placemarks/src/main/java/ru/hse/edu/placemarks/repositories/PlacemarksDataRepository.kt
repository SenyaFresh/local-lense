package ru.hse.edu.placemarks.repositories

import kotlinx.coroutines.flow.Flow
import ru.hse.edu.placemarks.entities.PlacemarkDataEntity
import ru.hse.locallense.common.ResultContainer

interface PlacemarksDataRepository {

    suspend fun getPlacemarks(): Flow<ResultContainer<List<PlacemarkDataEntity>>>

    suspend fun addPlacemark(placemark: PlacemarkDataEntity)

    suspend fun deletePlacemark(id: Long)

}