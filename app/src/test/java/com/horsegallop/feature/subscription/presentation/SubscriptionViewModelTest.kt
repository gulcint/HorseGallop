package com.horsegallop.feature.subscription.presentation

import com.horsegallop.data.billing.BillingManager
import com.horsegallop.data.billing.PurchaseState
import com.horsegallop.domain.subscription.model.SubscriptionStatus
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.domain.subscription.usecase.ObserveSubscriptionStatusUseCase
import com.horsegallop.domain.subscription.usecase.RestorePurchasesUseCase
import com.horsegallop.domain.subscription.usecase.StartSubscriptionPurchaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val observeSubscriptionStatusUseCase: ObserveSubscriptionStatusUseCase = mock()
    private val startSubscriptionPurchaseUseCase: StartSubscriptionPurchaseUseCase = mock()
    private val restorePurchasesUseCase: RestorePurchasesUseCase = mock()
    private val billingManager: BillingManager = mock()

    private val purchaseStateFlow = MutableStateFlow<PurchaseState>(PurchaseState.Idle)

    private val defaultStatus = SubscriptionStatus(
        tier = SubscriptionTier.FREE,
        isActive = true,
        expiresAtEpochMillis = null
    )

    private lateinit var viewModel: SubscriptionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(observeSubscriptionStatusUseCase()).thenReturn(flowOf(defaultStatus))
        whenever(billingManager.purchaseState).thenReturn(purchaseStateFlow)
        viewModel = SubscriptionViewModel(
            observeSubscriptionStatusUseCase,
            startSubscriptionPurchaseUseCase,
            restorePurchasesUseCase,
            billingManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── purchase success ────────────────────────────────────────────────────

    @Test
    fun `purchase success sets purchaseSuccess true`() = runTest {
        whenever(startSubscriptionPurchaseUseCase("horsegallop_pro_monthly", "token_abc"))
            .thenReturn(Result.success(Unit))

        purchaseStateFlow.value = PurchaseState.Purchased(
            productId = "horsegallop_pro_monthly",
            purchaseToken = "token_abc"
        )
        advanceUntilIdle()

        assertTrue(viewModel.ui.value.purchaseSuccess)
    }

    @Test
    fun `purchase success clears isPurchasing flag`() = runTest {
        whenever(startSubscriptionPurchaseUseCase("horsegallop_pro_monthly", "token_abc"))
            .thenReturn(Result.success(Unit))

        purchaseStateFlow.value = PurchaseState.Purchased(
            productId = "horsegallop_pro_monthly",
            purchaseToken = "token_abc"
        )
        advanceUntilIdle()

        val state = viewModel.ui.value
        assertTrue(state.purchaseSuccess)
        assertEquals(false, state.isPurchasing)
    }

    // ─── purchase failure ────────────────────────────────────────────────────

    @Test
    fun `purchase failure sets error state`() = runTest {
        whenever(startSubscriptionPurchaseUseCase("horsegallop_pro_monthly", "bad_token"))
            .thenReturn(Result.failure(RuntimeException("Verification failed")))

        purchaseStateFlow.value = PurchaseState.Purchased(
            productId = "horsegallop_pro_monthly",
            purchaseToken = "bad_token"
        )
        advanceUntilIdle()

        assertNotNull(viewModel.ui.value.error)
        assertEquals("purchase_verification_failed", viewModel.ui.value.error)
    }

    @Test
    fun `billing error state sets error and clears isPurchasing`() = runTest {
        purchaseStateFlow.value = PurchaseState.Error("Play Store error")
        advanceUntilIdle()

        val state = viewModel.ui.value
        assertNotNull(state.error)
        assertEquals("purchase_failed", state.error)
        assertEquals(false, state.isPurchasing)
    }

    // ─── clearError ──────────────────────────────────────────────────────────

    @Test
    fun `clearError resets error to null`() = runTest {
        whenever(startSubscriptionPurchaseUseCase("horsegallop_pro_monthly", "bad_token"))
            .thenReturn(Result.failure(RuntimeException("Fail")))

        purchaseStateFlow.value = PurchaseState.Purchased(
            productId = "horsegallop_pro_monthly",
            purchaseToken = "bad_token"
        )
        advanceUntilIdle()
        assertNotNull(viewModel.ui.value.error)

        viewModel.clearError()

        assertNull(viewModel.ui.value.error)
    }

    // ─── setError ────────────────────────────────────────────────────────────

    @Test
    fun `setError sets error message`() = runTest {
        advanceUntilIdle()

        viewModel.setError("billing_not_available")

        assertEquals("billing_not_available", viewModel.ui.value.error)
    }

    // ─── selectPlan ──────────────────────────────────────────────────────────

    @Test
    fun `selectPlan updates selectedPlan in state`() = runTest {
        advanceUntilIdle()

        viewModel.selectPlan(SubscriptionPlan.MONTHLY)

        assertEquals(SubscriptionPlan.MONTHLY, viewModel.ui.value.selectedPlan)
    }

    @Test
    fun `default selectedPlan is YEARLY`() = runTest {
        advanceUntilIdle()

        assertEquals(SubscriptionPlan.YEARLY, viewModel.ui.value.selectedPlan)
    }

    // ─── restorePurchases ────────────────────────────────────────────────────

    @Test
    fun `restorePurchases success updates status and clears isRestoring`() = runTest {
        val proStatus = SubscriptionStatus(
            tier = SubscriptionTier.PRO_MONTHLY,
            isActive = true,
            expiresAtEpochMillis = System.currentTimeMillis() + 30L * 24L * 60L * 60L * 1000L
        )
        whenever(restorePurchasesUseCase()).thenReturn(Result.success(proStatus))
        advanceUntilIdle()

        viewModel.restorePurchases()
        advanceUntilIdle()

        val state = viewModel.ui.value
        assertEquals(false, state.isRestoring)
        assertEquals(proStatus, state.status)
        assertNull(state.error)
    }

    @Test
    fun `restorePurchases failure sets error and clears isRestoring`() = runTest {
        whenever(restorePurchasesUseCase()).thenReturn(Result.failure(RuntimeException("Restore failed")))
        advanceUntilIdle()

        viewModel.restorePurchases()
        advanceUntilIdle()

        val state = viewModel.ui.value
        assertEquals(false, state.isRestoring)
        assertEquals("restore_failed", state.error)
    }

    // ─── billing cancelled ───────────────────────────────────────────────────

    @Test
    fun `billing cancelled clears isPurchasing without setting error`() = runTest {
        purchaseStateFlow.value = PurchaseState.Cancelled
        advanceUntilIdle()

        val state = viewModel.ui.value
        assertEquals(false, state.isPurchasing)
        assertNull(state.error)
    }

    // ─── observeSubscriptionStatus ───────────────────────────────────────────

    @Test
    fun `init observes subscription status and sets it in state`() = runTest {
        advanceUntilIdle()

        assertEquals(defaultStatus, viewModel.ui.value.status)
    }

    // ─── startSubscriptionPurchaseUseCase invocation ────────────────────────

    @Test
    fun `PurchaseState Purchased triggers startSubscriptionPurchaseUseCase with correct args`() = runTest {
        whenever(startSubscriptionPurchaseUseCase("horsegallop_pro_yearly", "yearly_token"))
            .thenReturn(Result.success(Unit))

        purchaseStateFlow.value = PurchaseState.Purchased(
            productId = "horsegallop_pro_yearly",
            purchaseToken = "yearly_token"
        )
        advanceUntilIdle()

        verify(startSubscriptionPurchaseUseCase).invoke("horsegallop_pro_yearly", "yearly_token")
    }
}
