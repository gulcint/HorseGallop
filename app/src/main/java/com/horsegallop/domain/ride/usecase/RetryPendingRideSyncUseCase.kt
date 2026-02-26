package com.horsegallop.domain.ride.usecase

import com.horsegallop.domain.ride.repository.RideRepository
import javax.inject.Inject

class RetryPendingRideSyncUseCase @Inject constructor(
    private val rideRepository: RideRepository
) {
    suspend operator fun invoke() = rideRepository.retryPendingRideSync()
}
