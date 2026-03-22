package com.horsegallop.feature.training.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.subscription.model.SubscriptionStatus
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.domain.subscription.usecase.ObserveSubscriptionStatusUseCase
import com.horsegallop.domain.training.model.TrainingPlan
import com.horsegallop.domain.training.usecase.CompleteTrainingTaskUseCase
import com.horsegallop.domain.training.usecase.GetTrainingPlansUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TrainingPlansViewModel @Inject constructor(
    private val getTrainingPlansUseCase: GetTrainingPlansUseCase,
    private val completeTrainingTaskUseCase: CompleteTrainingTaskUseCase,
    private val observeSubscriptionStatusUseCase: ObserveSubscriptionStatusUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(TrainingPlansUiState())
    val ui: StateFlow<TrainingPlansUiState> = _ui

    init {
        observeSubscriptionStatusUseCase()
            .onEach { status -> _ui.update { it.copy(subscription = status) } }
            .launchIn(viewModelScope)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            getTrainingPlansUseCase()
                .onSuccess { plans ->
                    _ui.update { it.copy(isLoading = false, plans = plans, error = null) }
                }
                .onFailure { error ->
                    _ui.update { it.copy(isLoading = false, error = "Veriler yüklenemedi. Lütfen tekrar deneyin.") }
                }
        }
    }

    fun completeTask(planId: String, taskId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isCompleting = true, error = null) }
            completeTrainingTaskUseCase(planId, taskId)
                .onSuccess {
                    _ui.update { it.copy(isCompleting = false) }
                    refresh()
                }
                .onFailure { error ->
                    _ui.update { it.copy(isCompleting = false, error = "Veriler yüklenemedi. Lütfen tekrar deneyin.") }
                }
        }
    }

    fun clearError() {
        _ui.update { it.copy(error = null) }
    }
}

data class TrainingPlansUiState(
    val isLoading: Boolean = true,
    val isCompleting: Boolean = false,
    val plans: List<TrainingPlan> = emptyList(),
    val subscription: SubscriptionStatus = SubscriptionStatus(
        tier = SubscriptionTier.FREE,
        isActive = true
    ),
    val error: String? = null
)
