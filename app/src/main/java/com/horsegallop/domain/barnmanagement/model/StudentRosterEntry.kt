package com.horsegallop.domain.barnmanagement.model

data class StudentRosterEntry(
    val userId: String,
    val displayName: String,
    val email: String,
    val reservationId: String,
    val bookedAtMs: Long
)
