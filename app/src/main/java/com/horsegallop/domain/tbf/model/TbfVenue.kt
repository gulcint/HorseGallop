package com.horsegallop.domain.tbf.model

data class TbfVenue(
    val code: String,
    val name: String,
    val eventCount: Int,
    val time: String = ""
)
