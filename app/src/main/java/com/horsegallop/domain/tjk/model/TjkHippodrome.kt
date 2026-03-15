package com.horsegallop.domain.tjk.model

data class TjkHippodrome(
    val code: String,
    val name: String,
    val raceCount: Int,
    val time: String = ""
)
