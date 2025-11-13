package com.horsegallop.ui.horses

import androidx.lifecycle.ViewModel
import com.horsegallop.domain.model.Horse
import com.horsegallop.domain.repository.HorseRepository
import kotlinx.coroutines.flow.Flow

class HorsesViewModel(
  private val horseRepository: HorseRepository
) : ViewModel() {
  val horses: Flow<List<Horse>> = horseRepository.getHorses()
}


