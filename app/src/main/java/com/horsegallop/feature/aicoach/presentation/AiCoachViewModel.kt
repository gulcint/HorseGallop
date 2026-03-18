package com.horsegallop.feature.aicoach.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.aicoach.model.ChatMessage
import com.horsegallop.domain.aicoach.usecase.AskAiCoachUseCase
import com.horsegallop.domain.ride.model.RideSession
import com.horsegallop.domain.ride.repository.RideHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AiCoachUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val rideContext: String? = null
)

@HiltViewModel
class AiCoachViewModel @Inject constructor(
    private val askAiCoachUseCase: AskAiCoachUseCase,
    private val rideHistoryRepository: RideHistoryRepository
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

        rideHistoryRepository.getRideHistory()
            .onEach { sessions ->
                val lastRide = sessions.firstOrNull()
                if (lastRide != null) {
                    _ui.update { it.copy(rideContext = buildRideContext(lastRide)) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun buildRideContext(ride: RideSession): String {
        val durationMin = ride.durationSec / 60
        return "Son sürüş: ${ride.distanceKm} km, $durationMin dk, " +
                "ortalama ${ride.avgSpeedKmh} km/s."
    }

    fun onInputChange(text: String) {
        _ui.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val question = _ui.value.inputText.trim()
        if (question.isEmpty() || _ui.value.isLoading) return

        val rideContext = _ui.value.rideContext
        val isFirstUserMessage = _ui.value.messages.none { it.role == ChatMessage.Role.USER }
        val fullMessage = if (rideContext != null && isFirstUserMessage) {
            "Bağlam: $rideContext\n\nKullanıcı sorusu: $question"
        } else {
            question
        }

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
            askAiCoachUseCase(fullMessage, _ui.value.messages.takeLast(10))
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
