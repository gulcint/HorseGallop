package com.horsegallop.domain.model.content

data class LoginContent(
  val headline: String,
  val description: String,
  val continueWithGoogleText: String?,
  val continueWithAppleText: String?,
  val continueWithEmailText: String?,
  val footerDescription: String?
)


