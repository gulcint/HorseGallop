package com.gallop.data.remote.dto

data class AppConfigDto(
  val theme: ThemeConfigDto,
  val featureFlags: Map<String, Boolean>,
  val announcementsEnabled: Boolean
)


