package com.horsegallop.feature.training.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.R
import com.horsegallop.domain.subscription.model.SubscriptionStatus
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.domain.subscription.repository.SubscriptionRepository
import com.horsegallop.domain.subscription.usecase.ObserveSubscriptionStatusUseCase
import com.horsegallop.domain.subscription.usecase.StartSubscriptionPurchaseUseCase
import com.horsegallop.domain.training.model.TrainingPlan
import com.horsegallop.domain.training.usecase.CompleteTrainingTaskUseCase
import com.horsegallop.domain.training.usecase.GetTrainingPlansUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrainingPlansUiState(
    val isLoading: Boolean = true,
    val isPurchasing: Boolean = false,
    val plans: List<TrainingPlan> = emptyList(),
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus(
        tier = SubscriptionTier.FREE,
        isActive = false
    ),
    val infoMessageResId: Int? = null,
    val errorMessageResId: Int? = null
)

@HiltViewModel
class TrainingPlansViewModel @Inject constructor(
    private val getTrainingPlansUseCase: GetTrainingPlansUseCase,
    private val completeTrainingTaskUseCase: CompleteTrainingTaskUseCase,
    private val observeSubscriptionStatusUseCase: ObserveSubscriptionStatusUseCase,
    private val startSubscriptionPurchaseUseCase: StartSubscriptionPurchaseUseCase,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingPlansUiState())
    val uiState: StateFlow<TrainingPlansUiState> = _uiState.asStateFlow()

    init {
        observeSubscription()
        refreshEntitlements()
        loadPlans()
    }

    fun loadPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val plansResult = getTrainingPlansUseCase()
            _uiState.update {
                plansResult.fold(
                    onSuccess = { plans ->
                        it.copy(isLoading = false, plans = plans, errorMessageResId = null)
                    },
                    onFailure = { _ ->
                        it.copy(isLoading = false, errorMessageResId = R.string.training_load_failed)
                    }
                )
            }
        }
    }

    fun onCompleteTask(planId: String, taskId: String) {
        viewModelScope.launch {
            val result = completeTrainingTaskUseCase(planId, taskId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        infoMessageResId = R.string.training_task_completed,
                        errorMessageResId = null
                    )
                }
                loadPlans()
            } else {
                val error = result.exceptionOrNull()
                val messageRes = if (error?.message == "pro_required") {
                    R.string.training_pro_required
                } else {
                    R.string.training_action_failed
                }
                _uiState.update { it.copy(errorMessageResId = messageRes) }
            }
        }
    }

    fun onStartPurchase(productId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true) }
            val result = startSubscriptionPurchaseUseCase(productId)
            val messageRes = if (result.isSuccess) {
                R.string.subscription_purchase_started
            } else {
                R.string.subscription_purchase_failed
            }
            _uiState.update {
                it.copy(
                    isPurchasing = false,
                    infoMessageResId = if (result.isSuccess) messageRes else null,
                    errorMessageResId = if (result.isSuccess) null else messageRes
                )
            }
            refreshEntitlements()
        }
    }

    fun consumeMessages() {
        _uiState.update { it.copy(infoMessageResId = null, errorMessageResId = null) }
    }

    private fun observeSubscription() {
        viewModelScope.launch {
            observeSubscriptionStatusUseCase().collect { status ->
                _uiState.update { it.copy(subscriptionStatus = status) }
            }
        }
    }

    private fun refreshEntitlements() {
        viewModelScope.launch {
            subscriptionRepository.refreshEntitlements()
        }
    }
}
