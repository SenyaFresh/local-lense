package ru.hse.edu.locallense.glue.photo

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import ru.hse.edu.placemarks.entities.PlacemarkType
import ru.hse.edu.placemarks.repositories.PlacemarksDataRepository
import ru.hse.locallense.common.ResultContainer
import java.io.File
import javax.inject.Inject

class PlacemarkPhotoCleanup @Inject constructor(
    private val placemarksDataRepository: PlacemarksDataRepository,
) {
    suspend fun deletePhotoFor(id: Long) {
        val list = withTimeoutOrNull(LOOKUP_TIMEOUT_MS) {
            placemarksDataRepository.getPlacemarks()
                .firstOrNull { it !is ResultContainer.Loading }
                ?.unwrapOrNull()
        }.orEmpty()
        val target = list.firstOrNull { it.placemark.id == id }?.placemark ?: return
        if (target.type != PlacemarkType.TEXT_PHOTO) return
        val path = target.contentSecondary ?: return
        runCatching { File(path).delete() }
    }

    private companion object {
        const val LOOKUP_TIMEOUT_MS = 1500L
    }
}
