package com.example.data.remote.dto

data class AuthRequestDto(
  val idToken: String
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
  val title: String,
  val titleTr: String? = null,
  val link: String? = null,
  val order: Int
)
