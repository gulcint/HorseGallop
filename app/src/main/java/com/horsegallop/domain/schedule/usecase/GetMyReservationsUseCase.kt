package com.horsegallop.domain.schedule.usecase

import com.horsegallop.domain.schedule.model.Reservation
import com.horsegallop.domain.schedule.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyReservationsUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    operator fun invoke(): Flow<List<Reservation>> = repository.getMyReservations()
}
