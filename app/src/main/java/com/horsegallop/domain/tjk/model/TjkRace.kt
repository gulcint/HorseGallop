package com.horsegallop.domain.tjk.model

data class TjkRace(
    val no: String,
    val name: String,
    val distance: Int,
    val surface: String,
    val time: String,
    val prize: Long,
    val horses: List<TjkHorse>
)
