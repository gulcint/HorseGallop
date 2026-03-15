package com.horsegallop.feature.aicoach.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.aicoach.model.ChatMessage
import com.horsegallop.domain.aicoach.usecase.AskAiCoachUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AiCoachUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AiCoachViewModel @Inject constructor(
    private val askAiCoachUseCase: AskAiCoachUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(AiCoachUiState())
    val ui: StateFlow<AiCoachUiState> = _ui

    init {
        _ui.update {
            it.copy(
                messages = listOf(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        role = ChatMessage.Role.ASSISTANT,
                        text = "Merhaba! Ben HorseGallop\'un AI koçuyum. Atınızla ilgili antrenman, bakım veya sağlık konularında size yardımcı olabilirim. Ne sormak istersiniz?"
                    )
                )
            )
        }
    }

    fun onInputChange(text: String) {
        _ui.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val question = _ui.value.inputText.trim()
        if (question.isEmpty() || _ui.value.isLoading) return

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = ChatMessage.Role.USER,
            text = question
        )

        _ui.update {
            it.copy(
                messages = it.messages + userMsg,
                inputText = "",
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            askAiCoachUseCase(question, _ui.value.messages.takeLast(10))
                .onSuccess { answer ->
                    val assistantMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        role = ChatMessage.Role.ASSISTANT,
                        text = answer
                    )
                    _ui.update { it.copy(messages = it.messages + assistantMsg, isLoading = false) }
                }
                .onFailure { error ->
                    _ui.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun clearError() {
        _ui.update { it.copy(error = null) }
    }
}
