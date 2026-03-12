package com.horsegallop.domain.barn.model

data class Instructor(
    val id: String,
    val name: String,
    val photoUrl: String?,
    val specialty: String,
    val rating: Double
)
