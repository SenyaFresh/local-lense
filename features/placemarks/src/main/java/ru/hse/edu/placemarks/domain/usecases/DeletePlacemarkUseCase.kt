package ru.hse.edu.placemarks.domain.usecases

import ru.hse.edu.placemarks.domain.repositories.PlacemarksRepository
import javax.inject.Inject

class DeletePlacemarkUseCase @Inject constructor(
    private val repository: PlacemarksRepository,
) {
    suspend fun invoke(id: Long) = repository.deletePlacemark(id)
}