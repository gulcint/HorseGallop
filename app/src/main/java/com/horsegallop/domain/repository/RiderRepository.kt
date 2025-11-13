package com.horsegallop.domain.repository

import com.horsegallop.domain.model.RiderProfile
import kotlinx.coroutines.flow.Flow

interface RiderRepository {
  fun getRiders(): Flow<List<RiderProfile>>
  fun getRiderById(id: String): Flow<RiderProfile?>
}


