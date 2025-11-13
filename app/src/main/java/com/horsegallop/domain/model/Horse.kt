package com.horsegallop.domain.model

enum class HorseGender {
  MARE,
  STALLION,
  GELDING
}

data class Horse(
  val id: String,
  val name: String,
  val age: Int,
  val breed: String,
  val gender: HorseGender,
  val heightCm: Int
)


