package com.horsegallop.domain.ride.usecase

import com.horsegallop.domain.ride.model.RideMetrics
import com.horsegallop.domain.ride.repository.RideRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRideMetricsUseCase @Inject constructor(
    private val rideRepository: RideRepository
) {
    operator fun invoke(): Flow<RideMetrics> = rideRepository.rideMetrics
}
