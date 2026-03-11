package ru.hse.edu.ar.domain.usecases

import ru.hse.edu.ar.domain.repositories.ArPlacemarksRepository
import ru.hse.locallense.common.entities.Tag
import javax.inject.Inject

class AddTagUseCase @Inject constructor(
    private val repository: ArPlacemarksRepository,
) {
    suspend fun invoke(tag: Tag) = repository.addTag(tag)
}