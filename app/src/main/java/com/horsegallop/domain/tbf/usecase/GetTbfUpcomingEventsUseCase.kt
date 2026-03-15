package com.horsegallop.domain.tbf.usecase

import com.horsegallop.domain.tbf.model.TbfEventDay
import com.horsegallop.domain.tbf.repository.TbfRepository
import javax.inject.Inject

class GetTbfUpcomingEventsUseCase @Inject constructor(
    private val repository: TbfRepository
) {
    suspend operator fun invoke(): Result<List<TbfEventDay>> =
        repository.getUpcomingEvents()
}
