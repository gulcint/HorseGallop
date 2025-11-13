package com.horsegallop.data.mock

import com.horsegallop.domain.model.Horse
import com.horsegallop.domain.model.HorseGender

object FakeHorseData {
  val horses: List<Horse> = listOf(
    Horse(id = "H-001", name = "Apollo", age = 7, breed = "Arabian", gender = HorseGender.STALLION, heightCm = 155),
    Horse(id = "H-002", name = "Luna", age = 5, breed = "Thoroughbred", gender = HorseGender.MARE, heightCm = 160),
    Horse(id = "H-003", name = "Atlas", age = 9, breed = "Friesian", gender = HorseGender.GELDING, heightCm = 165),
    Horse(id = "H-004", name = "Iris", age = 6, breed = "Andalusian", gender = HorseGender.MARE, heightCm = 158)
  )
}


