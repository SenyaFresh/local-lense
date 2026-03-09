package ru.hse.edu.placemarks.sources.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.hse.edu.placemarks.entities.PlacemarkDataEntity

@Dao
interface PlacemarksDao {

    @Query("SELECT * FROM placemarks")
    suspend fun getPlacemarks(): List<PlacemarkDataEntity>

    @Insert(entity = PlacemarkDataEntity::class)
    suspend fun addPlacemark(placemark: PlacemarkDataEntity)

    @Query("DELETE FROM placemarks WHERE id = :id")
    suspend fun deletePlacemark(id: Long)
}