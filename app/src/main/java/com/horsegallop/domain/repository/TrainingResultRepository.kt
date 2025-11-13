package com.horsegallop.domain.repository

import com.horsegallop.domain.model.TrainingResult
import kotlinx.coroutines.flow.Flow

interface TrainingResultRepository {
  fun getResults(): Flow<List<TrainingResult>>
  fun getResultForSession(sessionId: String): Flow<TrainingResult?>
}


