package com.horsegallop.domain.tjk.model

data class TjkCity(
    val id: Int,
    val name: String
)

data class TjkRaceResult(
    val position: String,
    val horseName: String,
    val jockey: String,
    val trainer: String,
    val weight: String,
    val time: String
)

data class TjkRace(
    val raceNo: Int,
    val raceTitle: String,
    val distance: String,
    val surface: String,
    val startTime: String,
    val results: List<TjkRaceResult>
)

data class TjkRaceDay(
    val date: String,
    val cityId: Int,
    val cityName: String,
    val races: List<TjkRace>
)
