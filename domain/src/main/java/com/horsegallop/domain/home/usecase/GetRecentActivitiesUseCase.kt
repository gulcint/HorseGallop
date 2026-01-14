package com.horsegallop.domain.home.usecase

import com.horsegallop.domain.home.model.RideSession
import com.horsegallop.domain.home.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentActivitiesUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    operator fun invoke(userId: String, limit: Int = 5): Flow<Result<List<RideSession>>> {
        return repository.getRecentActivities(userId, limit)
    }
}
