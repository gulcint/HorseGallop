package com.horsegallop.data.aicoach.repository

import com.horsegallop.data.remote.dto.AiCoachMessageDto
import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.aicoach.model.ChatMessage
import com.horsegallop.domain.aicoach.repository.AiCoachRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiCoachRepositoryImpl @Inject constructor(
    private val functionsDataSource: AppFunctionsDataSource
) : AiCoachRepository {

    override suspend fun ask(question: String, conversationHistory: List<ChatMessage>): Result<String> = runCatching {
        val historyDto = conversationHistory.map {
            AiCoachMessageDto(
                role = if (it.role == ChatMessage.Role.USER) "user" else "assistant",
                text = it.text
            )
        }
        functionsDataSource.askAiCoach(question, historyDto).answer
    }

    override suspend fun getConversationHistory(): Result<List<ChatMessage>> = Result.success(emptyList())
}
