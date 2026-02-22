package com.horsegallop.data.remote.dto

data class AuthRequestDto(
  val idToken: String
)

data class LoginRequestDto(
  val email: String,
  val password: String
)

data class RegisterRequestDto(
  val name: String,
  val email: String,
  val password: String
)

data class UserDto(
  val id: String,
  val role: String,
  val name: String,
  val email: String,
  val locale: String?,
  val lastVisit: String?
)

data class AuthResponseDto(
  val accessToken: String,
  val refreshToken: String,
  val user: UserDto
)

data class SliderItemDto(
  val id: String,
  val imageUrl: String,
  val title: String, // Backend returns localized title based on Accept-Language
  val link: String? = null,
  val order: Int
)
