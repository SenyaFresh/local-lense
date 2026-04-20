package ru.hse.edu.placemarks.sources.room

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.hse.edu.placemarks.entities.PlacemarkDataEntity
import ru.hse.edu.placemarks.entities.PlacemarkTagCrossRef
import ru.hse.edu.placemarks.entities.TagDataEntity

@Database(
    entities = [
        PlacemarkDataEntity::class,
        TagDataEntity::class,
        PlacemarkTagCrossRef::class,
    ],
    version = 2,
)
abstract class PlacemarksDatabase : RoomDatabase() {

    abstract fun getPlacemarksDao(): PlacemarksDao

    abstract fun getTagsDao(): TagDao
}