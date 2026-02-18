package com.horsegallop.domain.horse.model

data class Horse(
    val id: String = "",
    val name: String = "",
    val breed: String = "",
    val age: Int = 0,
    val photoUrl: String? = null,
    val weight: Float = 0f,
    val lastFarrierDate: Long = 0L,
    val lastVetCheckDate: Long = 0L,
    val healthNotes: String = "",
    val ownerId: String = ""
)
