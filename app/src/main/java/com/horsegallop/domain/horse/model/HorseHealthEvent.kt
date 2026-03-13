package com.horsegallop.domain.horse.model

data class HorseHealthEvent(
    val id: String,
    val horseId: String,
    val type: HorseHealthEventType,
    val date: String,         // yyyy-MM-dd
    val notes: String = "",
    val createdAt: String = ""
)

enum class HorseHealthEventType(val displayNameResId: Int = 0) {
    FARRIER,      // Nalbant
    VACCINATION,  // Aşı
    DENTAL,       // Diş
    VET,          // Veteriner
    DEWORMING,    // Parazit
    OTHER;        // Diğer

    companion object {
        fun fromString(value: String): HorseHealthEventType =
            entries.firstOrNull { it.name == value } ?: OTHER
    }
}
