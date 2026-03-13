package com.horsegallop.domain.horse.model

data class HorseTip(
    val id: String,
    val title: String,
    val body: String,
    /** Raw category key from backend (e.g. "breed", "physiology", "care", "speed", "anatomy", "vision", "behavior") */
    val category: String = ""
)
