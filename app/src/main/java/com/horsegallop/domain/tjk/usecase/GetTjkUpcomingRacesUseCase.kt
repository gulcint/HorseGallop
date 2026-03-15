package com.horsegallop.domain.tjk.usecase

import com.horsegallop.domain.tjk.model.TjkRaceDay
import com.horsegallop.domain.tjk.repository.TjkRepository
import javax.inject.Inject

class GetTjkUpcomingRacesUseCase @Inject constructor(
    private val repository: TjkRepository
) {
    suspend operator fun invoke(): Result<List<TjkRaceDay>> =
        repository.getUpcomingRaces()
}
