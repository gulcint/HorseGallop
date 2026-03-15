package com.horsegallop.domain.tjk.usecase

import com.horsegallop.domain.tjk.model.TjkRaceCard
import com.horsegallop.domain.tjk.repository.TjkRepository
import javax.inject.Inject

class GetTjkRaceCardUseCase @Inject constructor(
    private val repository: TjkRepository
) {
    suspend operator fun invoke(date: String?, hippodrome: String, type: String): Result<TjkRaceCard> =
        repository.getRaceCard(date, hippodrome, type)
}
