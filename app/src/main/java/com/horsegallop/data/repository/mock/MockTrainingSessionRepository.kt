package com.horsegallop.data.repository.mock

import com.horsegallop.data.mock.FakeTrainingSessionData
import com.horsegallop.domain.model.TrainingSession
import com.horsegallop.domain.repository.TrainingSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class MockTrainingSessionRepository : TrainingSessionRepository {
  private val sessionsFlow: MutableStateFlow<List<TrainingSession>> = MutableStateFlow(FakeTrainingSessionData.sessions)
  override fun getSessions(): Flow<List<TrainingSession>> { return sessionsFlow }
  override fun getSessionById(id: String): Flow<TrainingSession?> { return sessionsFlow.map { it.firstOrNull { s -> s.id == id } } }
}


