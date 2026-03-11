package ru.hse.edu.placemarks.domain.usecases

import ru.hse.edu.placemarks.domain.repositories.PlacemarksRepository
import ru.hse.locallense.common.entities.Tag
import javax.inject.Inject

class AddTagUseCase @Inject constructor(
    private val repository: PlacemarksRepository,
) {
    suspend fun invoke(tag: Tag) = repository.addTag(tag)
}