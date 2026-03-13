package com.horsegallop.domain.tjk.usecase

import com.horsegallop.domain.tjk.model.TjkRaceDay
import com.horsegallop.domain.tjk.repository.TjkRepository
import javax.inject.Inject

class GetTjkRaceDayUseCase @Inject constructor(
    private val tjkRepository: TjkRepository
) {
    suspend operator fun invoke(date: String, cityId: Int): Result<TjkRaceDay> =
        tjkRepository.getRaceDay(date, cityId)
}
