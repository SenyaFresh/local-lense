package ru.hse.edu.placemarks.sources.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ru.hse.edu.placemarks.entities.PlacemarkDataEntity
import ru.hse.edu.placemarks.entities.PlacemarkTagCrossRef
import ru.hse.edu.placemarks.entities.PlacemarkWithTags

@Dao
interface PlacemarksDao {

    @Transaction
    @Query("SELECT * FROM placemarks")
    suspend fun getPlacemarks(): List<PlacemarkWithTags>

    @Insert(entity = PlacemarkDataEntity::class)
    suspend fun insertPlacemark(placemark: PlacemarkDataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTagRefs(refs: List<PlacemarkTagCrossRef>)

    @Transaction
    suspend fun addPlacemark(placemark: PlacemarkWithTags) {
        insertPlacemark(placemark.placemark)
        insertTagRefs(placemark.tags.map { PlacemarkTagCrossRef(placemark.placemark.id, it.id) })
    }

    @Transaction
    @Query("DELETE FROM placemarks WHERE id = :id")
    suspend fun deletePlacemark(id: Long)
}