package com.horsegallop.domain.safety.model

data class SafetySettings(
    val isEnabled: Boolean = false,
    val contacts: List<SafetyContact> = emptyList(),
    val autoAlarmMinutes: Int = 5
)
