package com.horsegallop.domain.ride.usecase

import com.horsegallop.domain.ride.repository.RideRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObservePendingRideSyncCountUseCase @Inject constructor(
    private val rideRepository: RideRepository
) {
    operator fun invoke(): Flow<Int> = rideRepository.pendingSyncCount
}
