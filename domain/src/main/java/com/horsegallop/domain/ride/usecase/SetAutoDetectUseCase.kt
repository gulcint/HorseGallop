package com.horsegallop.domain.ride.usecase

import com.horsegallop.domain.ride.repository.RideRepository
import javax.inject.Inject

class SetAutoDetectUseCase @Inject constructor(
    private val rideRepository: RideRepository
) {
    suspend operator fun invoke(enabled: Boolean) = rideRepository.setAutoDetect(enabled)
}
