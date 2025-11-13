package com.gallop.data.remote.dto

data class LoginContentDto(
  val headline: String,
  val description: String,
  val forgotPasswordText: String,
  val continueWithGoogleText: String?,
  val continueWithAppleText: String?,
  val continueWithEmailText: String?
)


