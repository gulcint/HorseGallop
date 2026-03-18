package com.horsegallop.feature.subscription.presentation

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.data.billing.BillingManager
import com.horsegallop.data.billing.PurchaseState
import com.horsegallop.domain.subscription.model.SubscriptionStatus
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.domain.subscription.usecase.ObserveSubscriptionStatusUseCase
import com.horsegallop.domain.subscription.usecase.RestorePurchasesUseCase
import com.horsegallop.domain.subscription.usecase.StartSubscriptionPurchaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SubscriptionPlan { MONTHLY, YEARLY }

data class SubscriptionUiState(
    val status: SubscriptionStatus = SubscriptionStatus(
        tier = SubscriptionTier.FREE,
        isActive = true
    ),
    val selectedPlan: SubscriptionPlan = SubscriptionPlan.YEARLY,
    val isPurchasing: Boolean = false,
    val isRestoring: Boolean = false,
    val purchaseSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val observeSubscriptionStatusUseCase: ObserveSubscriptionStatusUseCase,
    private val startSubscriptionPurchaseUseCase: StartSubscriptionPurchaseUseCase,
    private val restorePurchasesUseCase: RestorePurchasesUseCase,
    private val billingManager: BillingManager
) : ViewModel() {

    private val _ui = MutableStateFlow(SubscriptionUiState())
    val ui: StateFlow<SubscriptionUiState> = _ui

    companion object {
        const val PRODUCT_PRO_MONTHLY = "horsegallop_pro_monthly"
        const val PRODUCT_PRO_YEARLY = "horsegallop_pro_yearly"
    }

    init {
        observeSubscriptionStatusUseCase()
            .onEach { status ->
                _ui.update { it.copy(status = status) }
            }
            .launchIn(viewModelScope)

        billingManager.purchaseState
            .onEach { state ->
                when (state) {
                    is PurchaseState.Purchased -> {
                        viewModelScope.launch {
                            startSubscriptionPurchaseUseCase(state.productId, state.purchaseToken)
                                .onSuccess { _ui.update { it.copy(isPurchasing = false, purchaseSuccess = true) } }
                                .onFailure { _ui.update { it.copy(isPurchasing = false, error = "purchase_verification_failed") } }
                        }
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
            .launchIn(viewModelScope)
    }

    fun selectPlan(plan: SubscriptionPlan) {
        _ui.update { it.copy(selectedPlan = plan) }
    }

    fun purchase(activity: Activity) {
        val productId = when (_ui.value.selectedPlan) {
            SubscriptionPlan.MONTHLY -> PRODUCT_PRO_MONTHLY
            SubscriptionPlan.YEARLY -> PRODUCT_PRO_YEARLY
        }
        _ui.update { it.copy(isPurchasing = true, error = null) }
        billingManager.launchBillingFlow(activity, productId)
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _ui.update { it.copy(isRestoring = true, error = null) }
            restorePurchasesUseCase()
                .onSuccess { status ->
                    _ui.update { it.copy(isRestoring = false, status = status) }
                }
                .onFailure {
                    _ui.update { it.copy(isRestoring = false, error = "restore_failed") }
                }
        }
    }

    fun clearError() {
        _ui.update { it.copy(error = null) }
    }

    fun setError(errorKey: String) {
        _ui.update { it.copy(error = errorKey) }
    }
}
