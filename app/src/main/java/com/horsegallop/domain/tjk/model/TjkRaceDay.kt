package com.horsegallop.domain.tjk.model

data class TjkRaceDay(
    val date: String,
    val type: String,
    val hippodromes: List<TjkHippodrome>
)
