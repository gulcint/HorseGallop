package com.horsegallop.domain.barn.model

data class BarnUi(
    val id: String,
    val name: String,
    val description: String,
    val location: String = "",
    val tags: List<String> = emptyList(),
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val heroImageUrl: String? = null,
    val ownerUserId: String? = null,
    val isFavorite: Boolean = false,
    val capacity: Int = 0,
    val phone: String? = null,
    val instructors: List<Instructor> = emptyList(),
    val recentReviews: List<BarnReview> = emptyList()
)
