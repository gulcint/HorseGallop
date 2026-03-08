package com.horsegallop.domain.horse.model

data class Horse(
    val id: String,
    val name: String,
    val breed: String = "",
    val birthYear: Int = 0,
    val color: String = "",
    val gender: HorseGender = HorseGender.UNKNOWN,
    val weightKg: Int = 0,
    val imageUrl: String = ""
) {
    val age: Int get() = if (birthYear > 0) (2026 - birthYear) else 0
}

enum class HorseGender(val displayName: String) {
    STALLION("Aygır"),
    MARE("Kısrak"),
    GELDING("İğdiş"),
    UNKNOWN("Bilinmiyor")
}
