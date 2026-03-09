package ru.hse.edu.locallense.glue.ar.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.hse.edu.ar.domain.entities.ArPlacemark
import ru.hse.edu.ar.domain.repositories.ArPlacemarksRepository
import ru.hse.edu.locallense.glue.ar.mappers.toArPlacemark
import ru.hse.edu.locallense.glue.ar.mappers.toPlacemarkDataEntity
import ru.hse.edu.placemarks.repositories.PlacemarksDataRepository
import ru.hse.locallense.common.ResultContainer
import javax.inject.Inject

class AdapterArPlacemarksRepository @Inject constructor(
    private val placemarksDataRepository: PlacemarksDataRepository,
): ArPlacemarksRepository {
    override suspend fun getPlacemarks(): Flow<ResultContainer<List<ArPlacemark>>> {
        return placemarksDataRepository.getPlacemarks().map { result ->
            result.map { list ->
                list.map { entity ->
                    entity.toArPlacemark()
                }
            }
        }
    }

    override suspend fun addPlacemark(placemark: ArPlacemark) {
        placemarksDataRepository.addPlacemark(placemark.toPlacemarkDataEntity())
    }

    override suspend fun deletePlacemark(id: Long) {
        placemarksDataRepository.deletePlacemark(id)
    }
}