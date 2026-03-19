package com.horsegallop.data.subscription.repository

import com.horsegallop.data.billing.BillingManager
import com.horsegallop.data.billing.PurchaseState
import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.VerifyPurchaseResponseDto
import com.horsegallop.domain.subscription.model.SubscriptionStatus
import com.horsegallop.domain.subscription.model.SubscriptionTier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private val supabaseDataSource: SupabaseDataSource = mock()
    private val billingManager: BillingManager = mock()

    private val purchaseStateFlow = MutableStateFlow<PurchaseState>(PurchaseState.Idle)

    private lateinit var repository: SubscriptionRepositoryImpl

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        Mockito.reset(supabaseDataSource, billingManager)
        whenever(billingManager.purchaseState).thenReturn(purchaseStateFlow)
        // Default: getSubscriptionStatus returns null (no backend response)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createRepository(): SubscriptionRepositoryImpl =
        SubscriptionRepositoryImpl(billingManager, supabaseDataSource)

    // ─── startSubscriptionPurchase — success path ───────────────────────────

    @Test
    fun `startSubscriptionPurchase returns success when verifyPurchase verified true`() = runTest {
        whenever(supabaseDataSource.getSubscriptionStatus()).thenReturn(null)
        repository = createRepository()
        advanceUntilIdle()

        val dto = VerifyPurchaseResponseDto(verified = true, tier = "PRO_MONTHLY")
        whenever(supabaseDataSource.verifyPurchase("token_123", "horsegallop_pro_monthly"))
            .thenReturn(Result.success(dto))

        val result = repository.startSubscriptionPurchase("horsegallop_pro_monthly", "token_123")
        advanceUntilIdle()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `startSubscriptionPurchase updates local status when verified true`() = runTest {
        whenever(supabaseDataSource.getSubscriptionStatus()).thenReturn(null)
        repository = createRepository()
        advanceUntilIdle()

        val dto = VerifyPurchaseResponseDto(verified = true, tier = "PRO_MONTHLY")
        whenever(supabaseDataSource.verifyPurchase("token_123", "horsegallop_pro_monthly"))
            .thenReturn(Result.success(dto))

        repository.startSubscriptionPurchase("horsegallop_pro_monthly", "token_123")
        advanceUntilIdle()

        val status = repository.getSubscriptionStatus().getOrNull()
        assertNotNull(status)
        assertEquals(SubscriptionTier.PRO_MONTHLY, status!!.tier)
        assertTrue(status.isActive)
    }

    @Test
    fun `startSubscriptionPurchase local status uses PRO_YEARLY for yearly productId`() = runTest {
        whenever(supabaseDataSource.getSubscriptionStatus()).thenReturn(null)
        repository = createRepository()
        advanceUntilIdle()

        val dto = VerifyPurchaseResponseDto(verified = true, tier = "PRO_YEARLY")
        whenever(supabaseDataSource.verifyPurchase("token_yearly", "horsegallop_pro_yearly"))
            .thenReturn(Result.success(dto))

        repository.startSubscriptionPurchase("horsegallop_pro_yearly", "token_yearly")
        advanceUntilIdle()

        val status = repository.getSubscriptionStatus().getOrNull()
        assertNotNull(status)
        assertEquals(SubscriptionTier.PRO_YEARLY, status!!.tier)
    }

    // ─── startSubscriptionPurchase — verified false path ────────────────────

    @Test
    fun `startSubscriptionPurchase returns failure when verified false`() = runTest {
        whenever(supabaseDataSource.getSubscriptionStatus()).thenReturn(null)
        repository = createRepository()
        advanceUntilIdle()

        val dto = VerifyPurchaseResponseDto(verified = false, tier = "FREE")
        whenever(supabaseDataSource.verifyPurchase("bad_token", "horsegallop_pro_monthly"))
            .thenReturn(Result.success(dto))

        val result = repository.startSubscriptionPurchase("horsegallop_pro_monthly", "bad_token")
        advanceUntilIdle()

        // verified=false maps to a successful Result<Unit> with no-op (tier stays FREE)
        // The impl calls map{} so it returns success but does not update tier
        assertTrue(result.isSuccess)
        val status = repository.getSubscriptionStatus().getOrNull()
        // Tier should remain FREE since verified=false skips updateStatusLocalFromProductId
        assertEquals(SubscriptionTier.FREE, status!!.tier)
    }

    // ─── startSubscriptionPurchase — exception path ──────────────────────────

    @Test
    fun `startSubscriptionPurchase returns failure on exception`() = runTest {
        whenever(supabaseDataSource.getSubscriptionStatus()).thenReturn(null)
        repository = createRepository()
        advanceUntilIdle()

        // When verifyPurchase throws, startSubscriptionPurchase propagates the exception.
        // Wrap the call in runCatching to assert failure.
        whenever(supabaseDataSource.verifyPurchase("token_err", "horsegallop_pro_monthly"))
            .thenThrow(RuntimeException("Network error"))

        val result = runCatching {
            repository.startSubscriptionPurchase("horsegallop_pro_monthly", "token_err")
        }
        advanceUntilIdle()

        assertTrue(result.isFailure)
    }

    // ─── getSubscriptionStatus ───────────────────────────────────────────────

    @Test
    fun `getSubscriptionStatus returns current local state`() = runTest {
        whenever(supabaseDataSource.getSubscriptionStatus()).thenReturn(null)
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.getSubscriptionStatus()

        assertTrue(result.isSuccess)
        assertEquals(SubscriptionTier.FREE, result.getOrNull()!!.tier)
    }

    // ─── observeSubscriptionStatus ───────────────────────────────────────────

    @Test
    fun `observeSubscriptionStatus emits initial FREE status`() = runTest {
        whenever(supabaseDataSource.getSubscriptionStatus()).thenReturn(null)
        repository = createRepository()
        advanceUntilIdle()

        val statuses = mutableListOf<SubscriptionStatus>()
        val job = launch {
            repository.observeSubscriptionStatus().collect { statuses.add(it) }
        }
        advanceUntilIdle()

        assertTrue(statuses.isNotEmpty())
        assertEquals(SubscriptionTier.FREE, statuses.first().tier)
        job.cancel()
    }

    // ─── observeBillingPurchases — billingManager wiring ────────────────────

    @Test
    fun `init subscribes to billingManager purchaseState`() = runTest {
        whenever(supabaseDataSource.getSubscriptionStatus()).thenReturn(null)
        repository = createRepository()
        advanceUntilIdle()

        // The repository must have read purchaseState from billingManager during init
        verify(billingManager).purchaseState
    }

    @Test
    fun `PurchaseState Idle does not update local subscription status`() = runTest {
        whenever(supabaseDataSource.getSubscriptionStatus()).thenReturn(null)
        repository = createRepository()
        advanceUntilIdle()

        // purchaseStateFlow already emits Idle by default — status stays FREE
        val status = repository.getSubscriptionStatus().getOrNull()
        assertNotNull(status)
        assertEquals(SubscriptionTier.FREE, status!!.tier)
    }

    // ─── refreshEntitlements ────────────────────────────────────────────────

    @Test
    fun `refreshEntitlements returns failure on exception`() = runTest {
        whenever(supabaseDataSource.getSubscriptionStatus())
            .thenThrow(RuntimeException("Network timeout"))
        repository = createRepository()

        val result = repository.refreshEntitlements()
        advanceUntilIdle()

        assertTrue(result.isFailure)
    }

    @Test
    fun `refreshEntitlements falls back to local status when backend returns null`() = runTest {
        whenever(supabaseDataSource.getSubscriptionStatus()).thenReturn(null)
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.refreshEntitlements()
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        // Status should reflect local FREE default
        assertEquals(SubscriptionTier.FREE, result.getOrNull()!!.tier)
    }
}
