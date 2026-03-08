package com.horsegallop.domain.horse.usecase

import com.horsegallop.domain.horse.model.Horse
import com.horsegallop.domain.horse.repository.HorseRepository
import javax.inject.Inject

class AddHorseUseCase @Inject constructor(private val repository: HorseRepository) {
    suspend operator fun invoke(horse: Horse): Result<Horse> = repository.addHorse(horse)
}
