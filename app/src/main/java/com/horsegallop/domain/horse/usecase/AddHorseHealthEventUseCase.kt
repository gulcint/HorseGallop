package com.horsegallop.domain.horse.usecase

import com.horsegallop.domain.horse.model.HorseHealthEvent
import com.horsegallop.domain.horse.model.HorseHealthEventType
import com.horsegallop.domain.horse.repository.HorseHealthRepository
import javax.inject.Inject

class AddHorseHealthEventUseCase @Inject constructor(
    private val repository: HorseHealthRepository
) {
    suspend operator fun invoke(
        horseId: String,
        type: HorseHealthEventType,
        date: String,
        notes: String
    ): Result<HorseHealthEvent> = repository.addHealthEvent(horseId, type, date, notes)
}
