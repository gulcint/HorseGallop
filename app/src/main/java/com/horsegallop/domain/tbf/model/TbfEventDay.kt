package com.horsegallop.domain.tbf.model

data class TbfEventDay(
    val date: String,
    val type: String,
    val venues: List<TbfVenue>
)
