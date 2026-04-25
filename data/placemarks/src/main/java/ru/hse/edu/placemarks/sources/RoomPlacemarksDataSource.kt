package ru.hse.edu.placemarks.sources

import android.content.Context
import androidx.room.Room
import ru.hse.edu.placemarks.entities.PlacemarkWithTags
import ru.hse.edu.placemarks.entities.TagDataEntity
import ru.hse.edu.placemarks.sources.room.PlacemarksDatabase
import javax.inject.Inject

class RoomPlacemarksDataSource @Inject constructor(
    context: Context,
) : PlacemarksDataSource {

    private val db: PlacemarksDatabase by lazy {
        Room.databaseBuilder(
            context,
            PlacemarksDatabase::class.java,
            "placemarks.db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()
    }

    override suspend fun getPlacemarks(): List<PlacemarkWithTags> {
        return db.getPlacemarksDao().getPlacemarks()
    }

    override suspend fun addPlacemark(placemark: PlacemarkWithTags) {
        db.getPlacemarksDao().addPlacemark(placemark)
    }

    override suspend fun deletePlacemark(id: Long) {
        db.getPlacemarksDao().deletePlacemark(id)
    }

    override suspend fun getTags(): List<TagDataEntity> {
        return db.getTagsDao().getTags()
    }

    override suspend fun addTag(tag: TagDataEntity) {
        db.getTagsDao().addTag(tag)
    }

    override suspend fun deleteTag(id: Long) {
        db.getTagsDao().deleteTag(id)
    }
}