package com.horsegallop.feature.home.domain.model

data class SliderItem(
  val id: String,
  val imageUrl: String,
  val title: String, // Localized title from backend based on Accept-Language header
  val link: String?,
  val order: Int
)
