package com.horsegallop.domain.aicoach.repository

import com.horsegallop.domain.aicoach.model.ChatMessage

interface AiCoachRepository {
    suspend fun ask(question: String, conversationHistory: List<ChatMessage>): Result<String>
    suspend fun getConversationHistory(): Result<List<ChatMessage>>
}
