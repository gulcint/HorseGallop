package com.horsegallop.domain.model.content

data class OnboardingPage(
  val title: String,
  val description: String,
  val imageUrl: String?
)

data class OnboardingContent(
  val pages: List<OnboardingPage>,
  val skipText: String?,
  val nextText: String?,
  val startText: String?
)


