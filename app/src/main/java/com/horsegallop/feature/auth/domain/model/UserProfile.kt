package com.horsegallop.feature.auth.domain.model

data class UserProfile(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val birthDate: String = "",
    val photoUrl: String? = null,
    val countryCode: String = "+90"
)
