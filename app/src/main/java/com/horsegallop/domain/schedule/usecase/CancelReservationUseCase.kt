package com.horsegallop.domain.schedule.usecase

import com.horsegallop.domain.schedule.repository.ScheduleRepository
import javax.inject.Inject

class CancelReservationUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(reservationId: String): Result<Unit> =
        repository.cancelReservation(reservationId)
}
