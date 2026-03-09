package ru.hse.edu.placemarks.sources

import android.content.Context
import androidx.room.Room
import ru.hse.edu.placemarks.entities.PlacemarkDataEntity
import ru.hse.edu.placemarks.sources.room.PlacemarksDatabase
import javax.inject.Inject

class RoomPlacemarksDataSource @Inject constructor(
    context: Context,
): PlacemarksDataSource {

    private val db: PlacemarksDatabase by lazy {
        Room.databaseBuilder(
            context,
            PlacemarksDatabase::class.java,
            "placemarks.db"
        ).build()
    }

    override suspend fun getPlacemarks(): List<PlacemarkDataEntity> {
        return db.getPlacemarksDao().getPlacemarks()
    }


    override suspend fun addPlacemark(placemark: PlacemarkDataEntity) {
        return db.getPlacemarksDao().addPlacemark(placemark)
    }

    override suspend fun deletePlacemark(id: Long) {
        return db.getPlacemarksDao().deletePlacemark(id)
    }
}