package com.gallop.data.remote.dto

data class NotificationDto(
  val id: String,
  val title: String,
  val body: String,
  val createdAtIso: String,
  val priority: String
)


