package com.horsegallop.domain.ride.usecase

import com.horsegallop.domain.ride.repository.RideRepository
import javax.inject.Inject

class StartRideUseCase @Inject constructor(
    private val rideRepository: RideRepository
) {
    suspend operator fun invoke(weightKg: Float = 70f) = rideRepository.startRide(weightKg)
}
