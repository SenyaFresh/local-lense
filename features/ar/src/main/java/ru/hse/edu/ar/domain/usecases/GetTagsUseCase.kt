package ru.hse.edu.ar.domain.usecases

import ru.hse.edu.ar.domain.repositories.ArPlacemarksRepository
import javax.inject.Inject

class GetTagsUseCase @Inject constructor(
    private val repository: ArPlacemarksRepository,
) {
    suspend fun invoke() = repository.getTags()
}