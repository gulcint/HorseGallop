package com.horsegallop.data.mock

import com.horsegallop.domain.model.Stable

object FakeStableData {
  val stables: List<Stable> = listOf(
    Stable(id = "S-001", name = "Sunrise Stable", location = "Istanbul", capacity = 24),
    Stable(id = "S-002", name = "Wind Valley", location = "Ankara", capacity = 16)
  )
}


