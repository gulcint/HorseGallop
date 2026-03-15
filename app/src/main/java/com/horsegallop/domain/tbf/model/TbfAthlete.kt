package com.horsegallop.domain.tbf.model

data class TbfAthlete(
    val no: String,
    val name: String,
    val jockey: String,
    val trainer: String,
    val owner: String,
    val weight: Int,
    val age: String,
    val last6: String,
    val odds: String,
    val bestTime: String,
    val result: String = "",
    val time: String = "",
    val gap: String = ""
)
