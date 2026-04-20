package ru.hse.edu.ar.domain.usecases

import ru.hse.edu.ar.domain.repositories.ArPlacemarksRepository
import javax.inject.Inject

class DeleteTagUseCase @Inject constructor(
    private val repository: ArPlacemarksRepository,
) {
    suspend fun invoke(id: Long) = repository.deleteTag(id)
}