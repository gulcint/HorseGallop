package com.horsegallop

import android.content.ContextWrapper
import com.horsegallop.domain.auth.AuthRepository
import com.horsegallop.domain.auth.model.UserProfile
import com.horsegallop.domain.model.User
import com.horsegallop.domain.model.UserRole
import com.horsegallop.domain.ride.model.RideMetrics
import com.horsegallop.domain.ride.model.StopRideResult
import com.horsegallop.domain.ride.repository.RideRepository
import com.horsegallop.domain.ride.usecase.RetryPendingRideSyncUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_triggersPendingRideRetry() = runTest {
        val fakeRideRepository = FakeRideRepository()
        val fakeAuthRepository = FakeAuthRepository()

        val viewModel = MainViewModel(
            authRepository = fakeAuthRepository,
            retryPendingRideSyncUseCase = RetryPendingRideSyncUseCase(fakeRideRepository),
            context = ContextWrapper(null)
        )

        advanceUntilIdle()

        assertEquals(1, fakeRideRepository.retryCallCount)
        assertTrue(viewModel.ui.value.isLoggedIn)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeRideRepository : RideRepository {
    val isRidingFlow = MutableStateFlow(false)
    val rideMetricsFlow = MutableStateFlow(RideMetrics())
    val pendingSyncCountFlow = MutableStateFlow(0)
    var retryCallCount = 0

    override val isRiding: Flow<Boolean> = isRidingFlow
    override val rideMetrics: Flow<RideMetrics> = rideMetricsFlow
    override val pendingSyncCount: Flow<Int> = pendingSyncCountFlow

    override suspend fun startRide(weightKg: Float, rideType: String?) = Unit

    override suspend fun stopRide(barnName: String?): StopRideResult = StopRideResult(
        localSaved = true,
        remoteSynced = true,
        pendingSyncId = null
    )

    override suspend fun retryPendingRideSync() {
        retryCallCount += 1
    }

    override suspend fun setAutoDetect(enabled: Boolean) = Unit
}

private class FakeAuthRepository : AuthRepository {
    override suspend fun signInWithGoogleIdToken(idToken: String) = Unit

    override fun isSignedIn(): Boolean = true

    override fun signOut() = Unit

    override fun signUpWithEmail(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Flow<Result<User>> = flowOf(
        Result.success(User("id", UserRole.CUSTOMER, "$firstName $lastName", email, true, null, null))
    )

    override fun signInWithEmail(email: String, password: String): Flow<Result<User>> =
        flowOf(Result.success(User("id", UserRole.CUSTOMER, "John Doe", email, true, null, null)))

    override fun sendPasswordResetEmail(email: String): Flow<Result<Unit>> = flowOf(Result.success(Unit))

    override fun confirmPasswordReset(code: String, newPassword: String): Flow<Result<Unit>> =
        flowOf(Result.success(Unit))

    override fun resendVerificationEmail(email: String?, password: String?): Flow<Result<Unit>> =
        flowOf(Result.success(Unit))

    override fun checkEmailVerified(): Flow<Result<Boolean>> = flowOf(Result.success(true))

    override fun saveUserToRemote(user: UserProfile): Flow<Result<Unit>> = flowOf(Result.success(Unit))

    override fun getLottieConfig(): Flow<Result<Pair<String, String>>> =
        flowOf(Result.success("" to ""))

    override fun getSplashTexts(locale: String): Flow<Result<Pair<String, String>>> =
        flowOf(Result.success("HorseGallop" to "Ready to ride"))

    override fun getCurrentUserId(): String? = "uid-main-test"
}
