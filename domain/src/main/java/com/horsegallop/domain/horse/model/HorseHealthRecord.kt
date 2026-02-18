package com.horsegallop.domain.horse.model

enum class RecordType {
    FARRIER, VETERINARY, VACCINATION, TRAINING, DEWORMING
}

data class HorseHealthRecord(
    val id: String = "",
    val horseId: String = "",
    val type: RecordType,
    val date: Long,
    val notes: String = "",
    val providerName: String = "",
    val cost: Double = 0.0
)
