package com.gallop.domain.model.remote

import java.time.Instant

data class Campaign(
  val id: String,
  val title: String,
  val message: String,
  val ctaText: String,
  val ctaUrl: String,
  val startsAt: Instant,
  val endsAt: Instant?,
  val imageUrl: String?
)


