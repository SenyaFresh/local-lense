package ru.hse.edu.placemarks.domain.usecases

import ru.hse.edu.placemarks.domain.repositories.PlacemarksRepository
import javax.inject.Inject

class GetPlacemarksUseCase @Inject constructor(
    private val repository: PlacemarksRepository,
) {
    suspend fun invoke() = repository.getPlacemarks()
}