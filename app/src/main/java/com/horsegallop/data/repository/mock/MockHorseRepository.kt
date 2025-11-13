package com.horsegallop.data.repository.mock

import com.horsegallop.data.mock.FakeHorseData
import com.horsegallop.domain.model.Horse
import com.horsegallop.domain.repository.HorseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class MockHorseRepository : HorseRepository {
  private val horsesFlow: MutableStateFlow<List<Horse>> = MutableStateFlow(FakeHorseData.horses)
  override fun getHorses(): Flow<List<Horse>> { return horsesFlow }
  override fun getHorseById(id: String): Flow<Horse?> { return horsesFlow.map { it.firstOrNull { h -> h.id == id } } }
}


