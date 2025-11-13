package com.gallop.domain.model.remote

import java.time.Instant

enum class NotificationPriority {
  LOW,
  NORMAL,
  HIGH
}

data class NotificationMessage(
  val id: String,
  val title: String,
  val body: String,
  val createdAt: Instant,
  val priority: NotificationPriority
)


