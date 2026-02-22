package com.horsegallop.domain.ride.repository

import com.horsegallop.domain.ride.model.RideSession
import kotlinx.coroutines.flow.Flow

interface RideHistoryRepository {
    fun getRideHistory(): Flow<List<RideSession>>
    fun getRide(id: String): Flow<RideSession?>
    suspend fun saveRide(ride: RideSession)
}
