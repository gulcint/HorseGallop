package com.horsegallop.domain.equestrian.usecase

import com.horsegallop.domain.equestrian.model.TbfActivity
import com.horsegallop.domain.equestrian.repository.TbfActivityRepository
import java.time.YearMonth
import javax.inject.Inject

class GetTbfActivitiesUseCase @Inject constructor(
    private val repository: TbfActivityRepository
) {
    suspend operator fun invoke(month: YearMonth): Result<List<TbfActivity>> =
        repository.getActivitiesForMonth(month)
}
