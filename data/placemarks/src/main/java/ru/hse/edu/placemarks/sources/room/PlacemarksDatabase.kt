package ru.hse.edu.placemarks.sources.room

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.hse.edu.placemarks.entities.PlacemarkDataEntity

@Database(
    entities = [
        PlacemarkDataEntity::class,
    ],
    version = 1,
)
abstract class PlacemarksDatabase: RoomDatabase() {

    abstract fun getPlacemarksDao(): PlacemarksDao

}