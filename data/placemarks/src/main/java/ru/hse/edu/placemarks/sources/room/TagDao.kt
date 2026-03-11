package ru.hse.edu.placemarks.sources.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.hse.edu.placemarks.entities.TagDataEntity

@Dao
interface TagDao {

    @Query("SELECT * FROM tags")
    suspend fun getTags(): List<TagDataEntity>

    @Insert(entity = TagDataEntity::class)
    suspend fun addTag(tag: TagDataEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTag(id: Long)
}