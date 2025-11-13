package com.horsegallop.data.repository.mock

import com.horsegallop.data.mock.FakeRiderData
import com.horsegallop.domain.model.RiderProfile
import com.horsegallop.domain.repository.RiderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class MockRiderRepository : RiderRepository {
  private val ridersFlow: MutableStateFlow<List<RiderProfile>> = MutableStateFlow(FakeRiderData.riders)
  override fun getRiders(): Flow<List<RiderProfile>> { return ridersFlow }
  override fun getRiderById(id: String): Flow<RiderProfile?> { return ridersFlow.map { it.firstOrNull { r -> r.id == id } } }
}


