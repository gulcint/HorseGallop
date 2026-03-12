package com.horsegallop.domain.horse.usecase

import com.horsegallop.domain.horse.model.HorseTip
import com.horsegallop.domain.horse.repository.HorseRepository
import javax.inject.Inject

class GetHorseTipsUseCase @Inject constructor(
    private val horseRepository: HorseRepository
) {
    suspend operator fun invoke(locale: String): Result<List<HorseTip>> =
        horseRepository.getHorseTips(locale)
}
