package com.example.domain.model

data class SliderItem(
  val id: String,
  val imageUrl: String,
  val title: String,
  val titleTr: String? = null, // Turkish title from backend
  val link: String?,
  val order: Int
)
