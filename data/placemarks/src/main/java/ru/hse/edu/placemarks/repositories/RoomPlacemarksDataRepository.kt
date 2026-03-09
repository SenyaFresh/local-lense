package ru.hse.edu.placemarks.repositories

import kotlinx.coroutines.flow.Flow
import ru.hse.edu.placemarks.entities.PlacemarkDataEntity
import ru.hse.edu.placemarks.sources.PlacemarksDataSource
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.common.flow.LazyFlowLoaderFactory
import javax.inject.Inject

class RoomPlacemarksDataRepository @Inject constructor(
    private val dataSource: PlacemarksDataSource,
    lazyFlowLoaderFactory: LazyFlowLoaderFactory,
): PlacemarksDataRepository {

    private val placemarksLoader = lazyFlowLoaderFactory.create {
        dataSource.getPlacemarks()
    }

    override suspend fun getPlacemarks(): Flow<ResultContainer<List<PlacemarkDataEntity>>> {
        return placemarksLoader.listen()
    }

    override suspend fun addPlacemark(placemark: PlacemarkDataEntity) {
        dataSource.addPlacemark(placemark)
        updateSources()
    }

    override suspend fun deletePlacemark(id: Long) {
        dataSource.deletePlacemark(id)
        updateSources()
    }

    private fun updateSources(silently: Boolean = false) {
        placemarksLoader.newAsyncLoad(
            valueLoader = { dataSource.getPlacemarks() },
            silently = silently
        )
    }
}