package com.horsegallop.feature.challenge.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.auth.usecase.GetCurrentUserIdUseCase
import com.horsegallop.domain.challenge.model.Badge
import com.horsegallop.domain.challenge.model.Challenge
import com.horsegallop.domain.challenge.usecase.GetActiveChallengesUseCase
import com.horsegallop.domain.challenge.usecase.GetEarnedBadgesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChallengeUiState(
    val loadingChallenges: Boolean = true,
    val loadingBadges: Boolean = true,
    val challenges: List<Challenge> = emptyList(),
    val badges: List<Badge> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val getActiveChallengesUseCase: GetActiveChallengesUseCase,
    private val getEarnedBadgesUseCase: GetEarnedBadgesUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(ChallengeUiState())
    val ui: StateFlow<ChallengeUiState> = _ui.asStateFlow()

    init {
        val userId = getCurrentUserIdUseCase().orEmpty()
        loadChallenges(userId)
        loadBadges(userId)
    }

    private fun loadChallenges(userId: String) {
        viewModelScope.launch {
            getActiveChallengesUseCase(userId)
                .catch { e -> _ui.update { it.copy(loadingChallenges = false, error = "Veriler yüklenemedi. Lütfen tekrar deneyin.") } }
                .collect { challenges ->
                    _ui.update { it.copy(loadingChallenges = false, challenges = challenges) }
                }
        }
    }

    private fun loadBadges(userId: String) {
        viewModelScope.launch {
            getEarnedBadgesUseCase(userId)
                .catch { e -> _ui.update { it.copy(loadingBadges = false, error = "Veriler yüklenemedi. Lütfen tekrar deneyin.") } }
                .collect { badges ->
                    _ui.update { it.copy(loadingBadges = false, badges = badges) }
                }
        }
    }

    fun clearError() = _ui.update { it.copy(error = null) }
}
