package com.horsegallop.domain.model

data class User(
  val id: String,
  val role: UserRole,
  val name: String,
  val email: String,
  val isEmailVerified: Boolean = false,
  val locale: String?,
  val lastVisitIso: String?
)
