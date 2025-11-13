package com.horsegallop.domain.repository

import com.horsegallop.domain.model.Stable
import kotlinx.coroutines.flow.Flow

interface StableRepository {
  fun getStables(): Flow<List<Stable>>
  fun getStableById(id: String): Flow<Stable?>
}


