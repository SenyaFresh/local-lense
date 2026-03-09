package ru.hse.edu.ar.domain.usecases

import ru.hse.edu.ar.domain.entities.ArPlacemark
import ru.hse.edu.ar.domain.repositories.ArPlacemarksRepository
import javax.inject.Inject

class AddPlacemarkUseCase @Inject constructor(
    private val repository: ArPlacemarksRepository,
) {
    suspend fun invoke(placemark: ArPlacemark) = repository.addPlacemark(placemark)
}