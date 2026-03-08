package com.horsegallop.domain.horse.usecase

import com.horsegallop.domain.horse.model.Horse
import com.horsegallop.domain.horse.repository.HorseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyHorsesUseCase @Inject constructor(private val repository: HorseRepository) {
    operator fun invoke(): Flow<List<Horse>> = repository.getMyHorses()
}
