package com.gallop.data.remote.dto

data class CampaignDto(
  val id: String,
  val title: String,
  val message: String,
  val ctaText: String,
  val ctaUrl: String,
  val startsAtIso: String,
  val endsAtIso: String?,
  val imageUrl: String?
)


