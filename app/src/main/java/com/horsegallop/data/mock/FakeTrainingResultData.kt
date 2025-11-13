package com.horsegallop.data.mock

import com.horsegallop.domain.model.TrainingResult

object FakeTrainingResultData {
  val results: List<TrainingResult> = listOf(
    TrainingResult(sessionId = "TS-001", performanceScore = 82, comments = "Balanced pace and good obedience.")
  )
}


