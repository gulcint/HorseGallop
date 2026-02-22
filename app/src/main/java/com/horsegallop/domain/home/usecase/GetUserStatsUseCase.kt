package com.horsegallop.domain.home.usecase

import com.horsegallop.domain.home.model.UserStats
import com.horsegallop.domain.home.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserStatsUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    operator fun invoke(userId: String): Flow<Result<UserStats>> {
        return repository.getUserStats(userId)
    }
}
