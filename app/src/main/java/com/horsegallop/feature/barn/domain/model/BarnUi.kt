package com.horsegallop.feature.barn.domain.model

data class BarnUi(
    val id: String,
    val name: String,
    val description: String,
    val location: String = "",
    val tags: List<String> = emptyList(),
    val lat: Double = 0.0,
    val lng: Double = 0.0
)
