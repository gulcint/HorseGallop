package com.horsegallop.data.repository.mock

import com.horsegallop.data.mock.FakeStableData
import com.horsegallop.domain.model.Stable
import com.horsegallop.domain.repository.StableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class MockStableRepository : StableRepository {
  private val stablesFlow: MutableStateFlow<List<Stable>> = MutableStateFlow(FakeStableData.stables)
  override fun getStables(): Flow<List<Stable>> { return stablesFlow }
  override fun getStableById(id: String): Flow<Stable?> { return stablesFlow.map { it.firstOrNull { s -> s.id == id } } }
}


