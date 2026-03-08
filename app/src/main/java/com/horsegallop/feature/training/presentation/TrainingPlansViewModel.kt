package com.horsegallop.feature.training.presentation

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.data.billing.BillingManager
import com.horsegallop.data.billing.PurchaseState
import com.horsegallop.domain.subscription.model.SubscriptionStatus
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.domain.subscription.usecase.ObserveSubscriptionStatusUseCase
import com.horsegallop.domain.subscription.usecase.StartSubscriptionPurchaseUseCase
import com.horsegallop.domain.training.model.TrainingPlan
import com.horsegallop.domain.training.usecase.CompleteTrainingTaskUseCase
import com.horsegallop.domain.training.usecase.GetTrainingPlansUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TrainingPlansViewModel @Inject constructor(
    private val getTrainingPlansUseCase: GetTrainingPlansUseCase,
    private val completeTrainingTaskUseCase: CompleteTrainingTaskUseCase,
    private val observeSubscriptionStatusUseCase: ObserveSubscriptionStatusUseCase,
    private val startSubscriptionPurchaseUseCase: StartSubscriptionPurchaseUseCase,
    private val billingManager: BillingManager
) : ViewModel() {

    private val _ui = MutableStateFlow(TrainingPlansUiState())
    val ui: StateFlow<TrainingPlansUiState> = _ui

    companion object {
        const val PRODUCT_PRO_MONTHLY = "horsegallop_pro_monthly"
        const val PRODUCT_PRO_YEARLY = "horsegallop_pro_yearly"
    }

    init {
        observeSubscription()
        observeBillingState()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            getTrainingPlansUseCase()
                .onSuccess { plans ->
                    _ui.update { state ->
                        state.copy(isLoading = false, plans = plans, error = null)
                    }
                }
                .onFailure { error ->
                    _ui.update { it.copy(isLoading = false, error = error.message ?: "unknown_error") }
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
                    _ui.update { it.copy(isCompleting = false, error = error.message ?: "unknown_error") }
                }
        }
    }

    fun upgradeToProMonthly(activity: Activity) {
        launchBillingFlow(activity, PRODUCT_PRO_MONTHLY)
    }

    fun upgradeToProYearly(activity: Activity) {
        launchBillingFlow(activity, PRODUCT_PRO_YEARLY)
    }

    fun clearError() {
        _ui.update { it.copy(error = null) }
    }

    private fun launchBillingFlow(activity: Activity, productId: String) {
        _ui.update { it.copy(isPurchasing = true, error = null) }
        billingManager.launchBillingFlow(activity, productId)
    }

    private fun observeSubscription() {
        viewModelScope.launch {
            observeSubscriptionStatusUseCase().collect { status ->
                _ui.update { it.copy(subscription = status) }
            }
        }
    }

    private fun observeBillingState() {
        viewModelScope.launch {
            billingManager.purchaseState.collect { state ->
                when (state) {
                    is PurchaseState.Purchased -> {
                        startSubscriptionPurchaseUseCase(state.productId)
                        _ui.update { it.copy(isPurchasing = false) }
                    }
                    is PurchaseState.Error -> {
                        _ui.update { it.copy(isPurchasing = false, error = "purchase_failed") }
                    }
                    is PurchaseState.Cancelled -> {
                        _ui.update { it.copy(isPurchasing = false) }
                    }
                    else -> {}
                }
            }
        }
    }
}

data class TrainingPlansUiState(
    val isLoading: Boolean = true,
    val isPurchasing: Boolean = false,
    val isCompleting: Boolean = false,
    val plans: List<TrainingPlan> = emptyList(),
    val subscription: SubscriptionStatus = SubscriptionStatus(
        tier = SubscriptionTier.FREE,
        isActive = true
    ),
    val error: String? = null
)
