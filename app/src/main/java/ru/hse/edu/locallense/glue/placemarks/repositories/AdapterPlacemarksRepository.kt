package ru.hse.edu.locallense.glue.placemarks.repositories

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.hse.edu.locallense.glue.ar.mappers.toTag
import ru.hse.edu.locallense.glue.ar.mappers.toTagDataEntity
import ru.hse.edu.locallense.glue.photo.PlacemarkPhotoCleanup
import ru.hse.edu.locallense.glue.placemarks.mappers.toPlacemark
import ru.hse.edu.placemarks.domain.entities.Placemark
import ru.hse.edu.placemarks.domain.repositories.PlacemarksRepository
import ru.hse.edu.placemarks.repositories.PlacemarksDataRepository
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.common.entities.Tag

class AdapterPlacemarksRepository @Inject constructor(
    private val placemarksDataRepository: PlacemarksDataRepository,
    private val photoCleanup: PlacemarkPhotoCleanup,
) : PlacemarksRepository {
    override suspend fun getPlacemarks(): Flow<ResultContainer<List<Placemark>>> {
        return placemarksDataRepository.getPlacemarks().map { result ->
            result.map { list ->
                list.map { entity ->
                    entity.toPlacemark()
                }
            }
        }
    }

    override suspend fun deletePlacemark(id: Long) {
        photoCleanup.deletePhotoFor(id)
        placemarksDataRepository.deletePlacemark(id)
    }

    override suspend fun getTags(): Flow<ResultContainer<List<Tag>>> {
        return placemarksDataRepository.getTags().map { result ->
            result.map { list ->
                list.map { entity ->
                    entity.toTag()
                }
            }
        }
    }

    override suspend fun addTag(tag: Tag) {
        placemarksDataRepository.addTag(tag.toTagDataEntity())
    }

    override suspend fun deleteTag(id: Long) {
        placemarksDataRepository.deleteTag(id)
    }
}