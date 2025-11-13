package com.horsegallop.domain.repository

import com.horsegallop.domain.model.Horse
import kotlinx.coroutines.flow.Flow

interface HorseRepository {
  fun getHorses(): Flow<List<Horse>>
  fun getHorseById(id: String): Flow<Horse?>
}


