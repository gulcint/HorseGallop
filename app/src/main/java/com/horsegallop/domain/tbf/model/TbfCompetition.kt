package com.horsegallop.domain.tbf.model

data class TbfCompetition(
    val no: String,
    val name: String,
    val distance: Int,
    val surface: String,
    val time: String,
    val prize: Long,
    val athletes: List<TbfAthlete>
)
