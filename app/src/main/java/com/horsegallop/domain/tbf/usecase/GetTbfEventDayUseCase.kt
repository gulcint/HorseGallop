package com.horsegallop.domain.tbf.usecase

import com.horsegallop.domain.tbf.model.TbfEventDay
import com.horsegallop.domain.tbf.repository.TbfRepository
import javax.inject.Inject

class GetTbfEventDayUseCase @Inject constructor(
    private val repository: TbfRepository
) {
    suspend operator fun invoke(date: String?, type: String): Result<TbfEventDay> =
        repository.getEventDay(date, type)
}
