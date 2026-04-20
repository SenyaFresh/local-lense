package ru.hse.edu.placemarks.repositories

import kotlinx.coroutines.flow.Flow
import ru.hse.edu.placemarks.entities.PlacemarkWithTags
import ru.hse.edu.placemarks.entities.TagDataEntity
import ru.hse.edu.placemarks.sources.PlacemarksDataSource
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.common.flow.LazyFlowLoaderFactory
import javax.inject.Inject

class RoomPlacemarksDataRepository @Inject constructor(
    private val dataSource: PlacemarksDataSource,
    lazyFlowLoaderFactory: LazyFlowLoaderFactory,
) : PlacemarksDataRepository {

    private val placemarksLoader = lazyFlowLoaderFactory.create {
        dataSource.getPlacemarks()
    }

    private val tagsLoader = lazyFlowLoaderFactory.create {
        dataSource.getTags()
    }

    override suspend fun getPlacemarks(): Flow<ResultContainer<List<PlacemarkWithTags>>> {
        return placemarksLoader.listen()
    }

    override suspend fun addPlacemark(placemark: PlacemarkWithTags) {
        dataSource.addPlacemark(placemark)
        updateSources()
    }

    override suspend fun deletePlacemark(id: Long) {
        dataSource.deletePlacemark(id)
        updateSources()
    }

    override suspend fun getTags(): Flow<ResultContainer<List<TagDataEntity>>> {
        return tagsLoader.listen()
    }

    override suspend fun addTag(tag: TagDataEntity) {
        dataSource.addTag(tag)
        updateSources()
    }

    override suspend fun deleteTag(id: Long) {
        dataSource.deleteTag(id)
        updateSources()
    }

    private fun updateSources(silently: Boolean = false) {
        placemarksLoader.newAsyncLoad(
            valueLoader = { dataSource.getPlacemarks() },
            silently = silently
        )
        tagsLoader.newAsyncLoad(
            valueLoader = { dataSource.getTags() },
            silently = silently
        )
    }
}