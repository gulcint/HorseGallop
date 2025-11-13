package com.horsegallop.domain.repository

import com.horsegallop.domain.model.TrainingSession
import kotlinx.coroutines.flow.Flow

interface TrainingSessionRepository {
  fun getSessions(): Flow<List<TrainingSession>>
  fun getSessionById(id: String): Flow<TrainingSession?>
}


