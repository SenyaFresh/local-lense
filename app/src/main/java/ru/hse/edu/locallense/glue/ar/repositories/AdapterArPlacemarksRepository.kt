package ru.hse.edu.locallense.glue.ar.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.hse.edu.ar.domain.entities.ArPlacemark
import ru.hse.edu.ar.domain.repositories.ArPlacemarksRepository
import ru.hse.edu.locallense.glue.ar.mappers.toArPlacemark
import ru.hse.edu.locallense.glue.ar.mappers.toPlacemarkWithTags
import ru.hse.edu.locallense.glue.ar.mappers.toTag
import ru.hse.edu.locallense.glue.ar.mappers.toTagDataEntity
import ru.hse.edu.placemarks.repositories.PlacemarksDataRepository
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.common.entities.Tag
import javax.inject.Inject

class AdapterArPlacemarksRepository @Inject constructor(
    private val placemarksDataRepository: PlacemarksDataRepository,
) : ArPlacemarksRepository {
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
        placemarksDataRepository.addPlacemark(placemark.toPlacemarkWithTags())
    }

    override suspend fun deletePlacemark(id: Long) {
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