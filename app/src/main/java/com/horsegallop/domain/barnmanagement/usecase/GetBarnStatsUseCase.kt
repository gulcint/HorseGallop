package com.horsegallop.domain.barnmanagement.usecase

import com.horsegallop.domain.barnmanagement.model.BarnStats
import com.horsegallop.domain.barnmanagement.repository.BarnManagementRepository
import javax.inject.Inject

class GetBarnStatsUseCase @Inject constructor(
    private val repository: BarnManagementRepository
) {
    suspend operator fun invoke(barnId: String): Result<BarnStats> =
        repository.getBarnStats(barnId)
}
