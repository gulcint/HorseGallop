package com.horsegallop.domain.barn.model

data class BarnReview(
    val id: String,
    val authorName: String,
    val rating: Int,
    val comment: String,
    val dateLabel: String
)
