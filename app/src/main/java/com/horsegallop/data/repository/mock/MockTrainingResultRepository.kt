package com.horsegallop.data.repository.mock

import com.horsegallop.data.mock.FakeTrainingResultData
import com.horsegallop.domain.model.TrainingResult
import com.horsegallop.domain.repository.TrainingResultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class MockTrainingResultRepository : TrainingResultRepository {
  private val resultsFlow: MutableStateFlow<List<TrainingResult>> = MutableStateFlow(FakeTrainingResultData.results)
  override fun getResults(): Flow<List<TrainingResult>> { return resultsFlow }
  override fun getResultForSession(sessionId: String): Flow<TrainingResult?> { return resultsFlow.map { it.firstOrNull { r -> r.sessionId == sessionId } } }
}


