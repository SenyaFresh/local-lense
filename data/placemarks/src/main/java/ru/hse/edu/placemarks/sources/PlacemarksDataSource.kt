package ru.hse.edu.placemarks.sources

import ru.hse.edu.placemarks.entities.PlacemarkDataEntity

interface PlacemarksDataSource {

    suspend fun getPlacemarks(): List<PlacemarkDataEntity>

    suspend fun addPlacemark(placemark: PlacemarkDataEntity)

    suspend fun deletePlacemark(id: Long)

}