package com.horsegallop.domain.tbf.model

data class TbfEventCard(
    val venue: String,
    val date: String,
    val type: String,
    val events: List<TbfCompetition>,
    val weather: String = "",
    val trackCondition: String = ""
)
