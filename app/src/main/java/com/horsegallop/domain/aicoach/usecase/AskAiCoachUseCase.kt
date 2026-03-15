package com.horsegallop.domain.aicoach.usecase

import com.horsegallop.domain.aicoach.model.ChatMessage
import com.horsegallop.domain.aicoach.repository.AiCoachRepository
import javax.inject.Inject

class AskAiCoachUseCase @Inject constructor(
    private val repository: AiCoachRepository
) {
    suspend operator fun invoke(question: String, history: List<ChatMessage>): Result<String> =
        repository.ask(question, history)
}
