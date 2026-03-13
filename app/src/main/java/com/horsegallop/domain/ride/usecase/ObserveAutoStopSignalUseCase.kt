package com.horsegallop.domain.ride.usecase

import com.horsegallop.domain.ride.repository.RideRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAutoStopSignalUseCase @Inject constructor(
    private val rideRepository: RideRepository
) {
    operator fun invoke(): Flow<Unit> = rideRepository.autoStopSignal
}
