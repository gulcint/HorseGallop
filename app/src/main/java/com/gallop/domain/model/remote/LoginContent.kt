package com.gallop.domain.model.remote

data class LoginContent(
  val headline: String,
  val description: String,
  val forgotPasswordText: String,
  val continueWithGoogleText: String?,
  val continueWithAppleText: String?,
  val continueWithEmailText: String?
)


