package com.horsegallop.domain.horse.usecase

import com.horsegallop.domain.horse.model.HorseHealthEvent
import com.horsegallop.domain.horse.repository.HorseHealthRepository
import javax.inject.Inject

class GetHorseHealthEventsUseCase @Inject constructor(
    private val repository: HorseHealthRepository
) {
    suspend operator fun invoke(horseId: String): Result<List<HorseHealthEvent>> =
        repository.getHealthEvents(horseId)
}
