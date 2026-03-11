package ru.hse.edu.placemarks.domain.usecases

import ru.hse.edu.placemarks.domain.repositories.PlacemarksRepository
import javax.inject.Inject

class GetTagsUseCase @Inject constructor(
    private val repository: PlacemarksRepository,
) {
    suspend fun invoke() = repository.getTags()
}