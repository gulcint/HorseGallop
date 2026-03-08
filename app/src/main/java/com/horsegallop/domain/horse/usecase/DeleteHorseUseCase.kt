package com.horsegallop.domain.horse.usecase

import com.horsegallop.domain.horse.repository.HorseRepository
import javax.inject.Inject

class DeleteHorseUseCase @Inject constructor(private val repository: HorseRepository) {
    suspend operator fun invoke(horseId: String): Result<Unit> = repository.deleteHorse(horseId)
}
