package com.horsegallop.domain.tbf.usecase

import com.horsegallop.domain.tbf.model.TbfEventCard
import com.horsegallop.domain.tbf.repository.TbfRepository
import javax.inject.Inject

class GetTbfEventCardUseCase @Inject constructor(
    private val repository: TbfRepository
) {
    suspend operator fun invoke(date: String?, venue: String, type: String): Result<TbfEventCard> =
        repository.getEventCard(date, venue, type)
}
