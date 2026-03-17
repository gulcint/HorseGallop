package com.horsegallop.domain.horse.usecase

import com.horsegallop.domain.horse.repository.HorseHealthRepository
import javax.inject.Inject

class DeleteHorseHealthEventUseCase @Inject constructor(
    private val repository: HorseHealthRepository
) {
    suspend operator fun invoke(id: String, horseId: String): Result<Unit> =
        repository.deleteHealthEvent(id, horseId)
}
