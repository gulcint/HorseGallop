package com.horsegallop.ui.training

import androidx.lifecycle.ViewModel
import com.horsegallop.domain.model.TrainingSession
import com.horsegallop.domain.repository.TrainingSessionRepository
import kotlinx.coroutines.flow.Flow

class TrainingSessionsViewModel(
  private val trainingSessionRepository: TrainingSessionRepository
) : ViewModel() {
  val sessions: Flow<List<TrainingSession>> = trainingSessionRepository.getSessions()
}


