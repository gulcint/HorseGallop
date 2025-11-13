package com.gallop.domain.model.remote

data class AppConfig(
  val theme: RemoteThemeConfig,
  val featureFlags: Map<String, Boolean>,
  val announcementsEnabled: Boolean
)


