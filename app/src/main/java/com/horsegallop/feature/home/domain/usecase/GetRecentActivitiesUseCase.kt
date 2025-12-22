package com.horsegallop.feature.home.domain.usecase

import com.horsegallop.feature.home.domain.model.RideSession
import com.horsegallop.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentActivitiesUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    operator fun invoke(userId: String, limit: Int = 5): Flow<Result<List<RideSession>>> {
        return repository.getRecentActivities(userId, limit)
    }
}
