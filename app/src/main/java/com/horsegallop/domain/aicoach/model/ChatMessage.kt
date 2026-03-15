package com.horsegallop.domain.aicoach.model

data class ChatMessage(
    val id: String,
    val role: Role,
    val text: String,
    val timestampMs: Long = System.currentTimeMillis()
) {
    enum class Role { USER, ASSISTANT }
}
