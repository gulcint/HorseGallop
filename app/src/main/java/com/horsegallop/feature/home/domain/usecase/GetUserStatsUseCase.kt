package com.horsegallop.feature.home.domain.usecase

import com.horsegallop.feature.home.domain.model.UserStats
import com.horsegallop.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserStatsUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    operator fun invoke(userId: String): Flow<Result<UserStats>> {
        return repository.getUserStats(userId)
    }
}
