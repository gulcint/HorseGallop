package com.horsegallop.domain.tjk.model

data class TjkRaceCard(
    val hippodrome: String,
    val date: String,
    val type: String,
    val races: List<TjkRace>,
    val weather: String = "",
    val trackCondition: String = ""
)
