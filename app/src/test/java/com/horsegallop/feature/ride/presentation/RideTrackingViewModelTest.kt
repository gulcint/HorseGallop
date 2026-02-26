package com.horsegallop.feature.ride.presentation

import android.net.Uri
import com.horsegallop.domain.auth.AuthRepository
import com.horsegallop.domain.auth.model.UserProfile
import com.horsegallop.domain.auth.repository.ProfileRepository
import com.horsegallop.domain.auth.usecase.GetCurrentUserIdUseCase
import com.horsegallop.domain.auth.usecase.GetUserProfileUseCase
import com.horsegallop.domain.barn.model.BarnUi
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.barn.repository.BarnRepository
import com.horsegallop.domain.model.User
import com.horsegallop.domain.model.UserRole
import com.horsegallop.domain.ride.model.GeoPoint
import com.horsegallop.domain.ride.model.RideMetrics
import com.horsegallop.domain.ride.model.RideSyncStatus
import com.horsegallop.domain.ride.model.StopRideResult
import com.horsegallop.domain.ride.repository.RideRepository
import com.horsegallop.domain.ride.usecase.ObserveIsRidingUseCase
import com.horsegallop.domain.ride.usecase.ObservePendingRideSyncCountUseCase
import com.horsegallop.domain.ride.usecase.ObserveRideMetricsUseCase
import com.horsegallop.domain.ride.usecase.RetryPendingRideSyncUseCase
import com.horsegallop.domain.ride.usecase.SetAutoDetectUseCase
import com.horsegallop.domain.ride.usecase.StartRideUseCase
import com.horsegallop.domain.ride.usecase.StopRideUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class RideTrackingViewModelTest {

    @get:Rule
    val mainDispatcherRule = RideMainDispatcherRule()

    @Test
    fun onToggleRide_start_passesSelectedRideTypeToUseCase() = runTest {
        val (viewModel, fakeRideRepository) = buildViewModel()

        viewModel.onRideTypeSelected(RideType.ENDURANCE)
        viewModel.onToggleRide(hasLocationPermission = true)
        advanceUntilIdle()

        assertEquals("endurance", fakeRideRepository.lastStartRideType)
        assertTrue(viewModel.uiState.value.isRiding)
    }

    @Test
    fun metricsFlow_updatesAverageSpeedCorrectly() = runTest {
        val (viewModel, fakeRideRepository) = buildViewModel()

        fakeRideRepository.metricsFlow.value = RideMetrics(
            speedKmh = 11f,
            distanceKm = 6f,
            durationSec = 1800,
            calories = 220,
            pathPoints = listOf(GeoPoint(41.0, 29.0), GeoPoint(41.01, 29.02))
        )
        fakeRideRepository.isRidingFlow.value = true
        advanceUntilIdle()

        assertEquals(12f, viewModel.uiState.value.avgSpeedKmh)
        assertEquals(11f, viewModel.uiState.value.speedKmh)
    }

    @Test
    fun onToggleRide_stop_createsSavedRideSummary() = runTest {
        val (viewModel, fakeRideRepository) = buildViewModel()

        viewModel.onRideTypeSelected(RideType.DRESSAGE)
        fakeRideRepository.metricsFlow.value = RideMetrics(
            speedKmh = 12f,
            distanceKm = 4.5f,
            durationSec = 1500,
            calories = 310,
            pathPoints = listOf(GeoPoint(41.0, 29.0), GeoPoint(41.02, 29.01))
        )
        fakeRideRepository.isRidingFlow.value = true
        advanceUntilIdle()

        viewModel.onToggleRide(hasLocationPermission = true)
        advanceUntilIdle()

        val summary = viewModel.uiState.value.savedRideSummary
        assertNotNull(summary)
        assertEquals(4.5f, summary?.distanceKm)
        assertEquals(310, summary?.calories)
        assertEquals(RideType.DRESSAGE, summary?.rideType)
        assertEquals("Caddebostan Arena", fakeRideRepository.lastStopBarnName)
    }

    @Test
    fun pendingSyncCount_updatesUiState() = runTest {
        val (viewModel, fakeRideRepository) = buildViewModel()

        fakeRideRepository.pendingSyncCountFlow.value = 2
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.pendingSyncCount)
    }

    @Test
    fun stopRide_withPendingSync_setsPendingStatus() = runTest {
        val (viewModel, fakeRideRepository) = buildViewModel()
        fakeRideRepository.stopRideResult = StopRideResult(
            localSaved = true,
            remoteSynced = false,
            pendingSyncId = "pending-1"
        )
        fakeRideRepository.metricsFlow.value = RideMetrics(
            speedKmh = 10f,
            distanceKm = 2.3f,
            durationSec = 600,
            calories = 140,
            pathPoints = listOf(GeoPoint(41.0, 29.0), GeoPoint(41.01, 29.01))
        )
        fakeRideRepository.isRidingFlow.value = true
        advanceUntilIdle()

        viewModel.onToggleRide(hasLocationPermission = true)
        advanceUntilIdle()

        assertEquals(RideSyncStatus.Pending, viewModel.uiState.value.lastStopSyncStatus)
    }

    @Test
    fun onRetryPendingSync_callsRepositoryRetry() = runTest {
        val (viewModel, fakeRideRepository) = buildViewModel()
        val initialRetryCount = fakeRideRepository.retryCallCount

        viewModel.onRetryPendingSync()
        advanceUntilIdle()

        assertTrue(fakeRideRepository.retryCallCount >= initialRetryCount + 1)
    }

    private fun buildViewModel(): Pair<RideTrackingViewModel, FakeRideRepository> {
        val fakeRideRepository = FakeRideRepository()
        val fakeBarnRepository = FakeBarnRepository()
        val fakeAuthRepository = FakeAuthRepository(currentUserId = "uid-1")
        val fakeProfileRepository = FakeProfileRepository()

        val viewModel = RideTrackingViewModel(
            startRideUseCase = StartRideUseCase(fakeRideRepository),
            stopRideUseCase = StopRideUseCase(fakeRideRepository),
            observeRideMetricsUseCase = ObserveRideMetricsUseCase(fakeRideRepository),
            observeIsRidingUseCase = ObserveIsRidingUseCase(fakeRideRepository),
            observePendingRideSyncCountUseCase = ObservePendingRideSyncCountUseCase(fakeRideRepository),
            retryPendingRideSyncUseCase = RetryPendingRideSyncUseCase(fakeRideRepository),
            setAutoDetectUseCase = SetAutoDetectUseCase(fakeRideRepository),
            barnRepository = fakeBarnRepository,
            getUserProfileUseCase = GetUserProfileUseCase(fakeProfileRepository),
            getCurrentUserIdUseCase = GetCurrentUserIdUseCase(fakeAuthRepository)
        )

        return viewModel to fakeRideRepository
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RideMainDispatcherRule(
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
    val metricsFlow = MutableStateFlow(RideMetrics())
    val pendingSyncCountFlow = MutableStateFlow(0)

    var lastStartRideType: String? = null
    var lastStopBarnName: String? = null
    var autoDetectValue: Boolean = false
    var stopRideResult: StopRideResult = StopRideResult(
        localSaved = true,
        remoteSynced = true,
        pendingSyncId = null
    )
    var retryCallCount: Int = 0

    override val isRiding: Flow<Boolean> = isRidingFlow
    override val rideMetrics: Flow<RideMetrics> = metricsFlow
    override val pendingSyncCount: Flow<Int> = pendingSyncCountFlow

    override suspend fun startRide(weightKg: Float, rideType: String?) {
        lastStartRideType = rideType
        isRidingFlow.value = true
    }

    override suspend fun stopRide(barnName: String?): StopRideResult {
        lastStopBarnName = barnName
        isRidingFlow.value = false
        return stopRideResult
    }

    override suspend fun retryPendingRideSync() {
        retryCallCount += 1
    }

    override suspend fun setAutoDetect(enabled: Boolean) {
        autoDetectValue = enabled
    }
}

private class FakeBarnRepository : BarnRepository {
    private val barns = listOf(
        BarnWithLocation(
            barn = BarnUi(
                id = "barn-1",
                name = "Caddebostan Arena",
                description = "Training barn"
            ),
            lat = 41.0,
            lng = 29.0,
            amenities = emptySet()
        )
    )

    override fun getBarns(): Flow<List<BarnWithLocation>> = flowOf(barns)

    override fun getBarnById(barnId: String): Flow<BarnWithLocation?> =
        flowOf(barns.firstOrNull { it.barn.id == barnId })

    override suspend fun toggleFavorite(barnId: String) = Unit
}

private class FakeProfileRepository : ProfileRepository {
    override fun getUserProfile(uid: String): Flow<Result<UserProfile>> {
        return flowOf(Result.success(UserProfile(weight = 72f)))
    }

    override fun updateUserProfile(uid: String, profile: UserProfile): Flow<Result<Unit>> = flow {
        emit(Result.success(Unit))
    }

    override fun updateProfileImage(uid: String, uri: Uri): Flow<Result<String>> =
        flowOf(Result.success("https://example.com/p.jpg"))

    override fun deleteAccount(): Flow<Result<Unit>> = flowOf(Result.success(Unit))

    override fun signOut() = Unit
}

private class FakeAuthRepository(
    private var currentUserId: String?
) : AuthRepository {

    override suspend fun signInWithGoogleIdToken(idToken: String) = Unit

    override fun isSignedIn(): Boolean = currentUserId != null

    override fun signOut() {
        currentUserId = null
    }

    override fun signUpWithEmail(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Flow<Result<User>> {
        return flowOf(Result.success(User("id", UserRole.CUSTOMER, "$firstName $lastName", email, true, null, null)))
    }

    override fun signInWithEmail(email: String, password: String): Flow<Result<User>> {
        return flowOf(Result.success(User("id", UserRole.CUSTOMER, "John Doe", email, true, null, null)))
    }

    override fun sendPasswordResetEmail(email: String): Flow<Result<Unit>> = flowOf(Result.success(Unit))

    override fun confirmPasswordReset(code: String, newPassword: String): Flow<Result<Unit>> =
        flowOf(Result.success(Unit))

    override fun resendVerificationEmail(email: String?, password: String?): Flow<Result<Unit>> =
        flowOf(Result.success(Unit))

    override fun checkEmailVerified(): Flow<Result<Boolean>> = flowOf(Result.success(true))

    override fun saveUserToRemote(user: UserProfile): Flow<Result<Unit>> = flowOf(Result.success(Unit))

    override fun getLottieConfig(): Flow<Result<Pair<String, String>>> = flowOf(Result.success("" to ""))

    override fun getSplashTexts(locale: String): Flow<Result<Pair<String, String>>> =
        flowOf(Result.success("" to ""))

    override fun getCurrentUserId(): String? = currentUserId
}
