package com.horsegallop.domain.tjk.usecase

import com.horsegallop.domain.tjk.model.TjkRaceDay
import com.horsegallop.domain.tjk.repository.TjkRepository
import javax.inject.Inject

class GetTjkRaceDayUseCase @Inject constructor(
    private val repository: TjkRepository
) {
    suspend operator fun invoke(date: String?, type: String): Result<TjkRaceDay> =
        repository.getRaceDay(date, type)
}
