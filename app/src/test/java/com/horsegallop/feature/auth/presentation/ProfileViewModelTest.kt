package com.horsegallop.feature.auth.presentation

import android.net.Uri
import com.horsegallop.R
import com.horsegallop.domain.auth.AuthRepository
import com.horsegallop.domain.auth.model.UserProfile
import com.horsegallop.domain.auth.repository.ProfileRepository
import com.horsegallop.domain.auth.usecase.GetCurrentUserIdUseCase
import com.horsegallop.domain.auth.usecase.GetUserProfileUseCase
import com.horsegallop.domain.auth.usecase.SignOutUseCase
import com.horsegallop.domain.auth.usecase.UpdateProfileImageUseCase
import com.horsegallop.domain.auth.usecase.UpdateUserProfileUseCase
import com.horsegallop.domain.model.User
import com.horsegallop.domain.model.UserRole
import com.horsegallop.domain.subscription.model.SubscriptionStatus
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.domain.subscription.repository.SubscriptionRepository
import com.horsegallop.domain.subscription.usecase.ObserveSubscriptionStatusUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun startEditSession_copiesCurrentProfileIntoDraft() = runTest {
        val seedProfile = UserProfile(
            firstName = "Ada",
            lastName = "Lovelace",
            email = "ada@example.com",
            phone = "5551234567",
            city = "Istanbul",
            birthDate = "1990-01-01",
            countryCode = "+90",
            weight = 64f
        )
        val (viewModel, _) = buildViewModel(seedProfile)

        advanceUntilIdle()
        viewModel.updateDraft(firstName = "Changed")
        viewModel.startEditSession(force = true)

        assertEquals(seedProfile.firstName, viewModel.uiState.value.draftProfile.firstName)
        assertEquals("64", viewModel.uiState.value.draftWeightInput)
    }

    @Test
    fun saveProfile_whenFirstNameBlank_setsValidationError() = runTest {
        val (viewModel, fakeProfileRepository) = buildViewModel()

        advanceUntilIdle()
        viewModel.startEditSession(force = true)
        viewModel.updateDraft(firstName = "")
        viewModel.saveProfile()

        advanceUntilIdle()
        assertEquals(
            R.string.validation_first_name_required,
            viewModel.uiState.value.formErrors.firstNameResId
        )
        assertNull(fakeProfileRepository.lastUpdatedProfile)
    }

    @Test
    fun saveProfile_whenPhoneInvalid_setsValidationError() = runTest {
        val (viewModel, fakeProfileRepository) = buildViewModel()

        advanceUntilIdle()
        viewModel.startEditSession(force = true)
        viewModel.updateDraft(phone = "12ab")
        viewModel.saveProfile()

        advanceUntilIdle()
        assertEquals(
            R.string.validation_phone_invalid,
            viewModel.uiState.value.formErrors.phoneResId
        )
        assertNull(fakeProfileRepository.lastUpdatedProfile)
    }

    @Test
    fun saveProfile_whenWeightOutOfRange_setsValidationError() = runTest {
        val (viewModel, fakeProfileRepository) = buildViewModel()

        advanceUntilIdle()
        viewModel.startEditSession(force = true)
        viewModel.updateDraft(weightInput = "700")
        viewModel.saveProfile()

        advanceUntilIdle()
        assertEquals(
            R.string.validation_weight_invalid,
            viewModel.uiState.value.formErrors.weightResId
        )
        assertNull(fakeProfileRepository.lastUpdatedProfile)
    }

    @Test
    fun saveProfile_whenValid_updatesRepositoryWithParsedWeight() = runTest {
        val (viewModel, fakeProfileRepository) = buildViewModel()

        advanceUntilIdle()
        viewModel.startEditSession(force = true)
        viewModel.updateDraft(
            firstName = "Ada",
            lastName = "Lovelace",
            phone = "5551234567",
            city = "Istanbul",
            birthDate = "1990-01-01",
            countryCode = "+90",
            weightInput = "72.5"
        )

        viewModel.saveProfile()

        advanceUntilIdle()
        assertEquals(72.5f, fakeProfileRepository.lastUpdatedProfile?.weight)
        assertEquals(R.string.profile_saved_success, viewModel.uiState.value.successMessageResId)
    }

    private fun buildViewModel(
        seedProfile: UserProfile = UserProfile(
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            phone = "5551234567",
            city = "Istanbul",
            birthDate = "1992-10-10",
            countryCode = "+90",
            weight = 70f
        )
    ): Pair<ProfileViewModel, FakeProfileRepository> {
        val fakeAuthRepository = FakeAuthRepository(currentUserId = "uid-1")
        val fakeProfileRepository = FakeProfileRepository(seedProfile)
        val fakeSubscriptionRepository = FakeSubscriptionRepository()

        val viewModel = ProfileViewModel(
            getCurrentUserIdUseCase = GetCurrentUserIdUseCase(fakeAuthRepository),
            getUserProfileUseCase = GetUserProfileUseCase(fakeProfileRepository),
            updateUserProfileUseCase = UpdateUserProfileUseCase(fakeProfileRepository),
            updateProfileImageUseCase = UpdateProfileImageUseCase(fakeProfileRepository),
            signOutUseCase = SignOutUseCase(fakeAuthRepository),
            observeSubscriptionStatusUseCase = ObserveSubscriptionStatusUseCase(fakeSubscriptionRepository)
        )

        return viewModel to fakeProfileRepository
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

private class FakeProfileRepository(
    profile: UserProfile
) : ProfileRepository {

    var currentProfile: UserProfile = profile
    var lastUpdatedProfile: UserProfile? = null

    override fun getUserProfile(uid: String): Flow<Result<UserProfile>> {
        return flowOf(Result.success(currentProfile))
    }

    override fun updateUserProfile(uid: String, profile: UserProfile): Flow<Result<Unit>> = flow {
        lastUpdatedProfile = profile
        currentProfile = profile
        emit(Result.success(Unit))
    }

    override fun updateProfileImage(uid: String, uri: Uri): Flow<Result<String>> = flowOf(Result.success("https://example.com/photo.jpg"))

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

    override fun signUpWithEmail(email: String, password: String, firstName: String, lastName: String): Flow<Result<User>> {
        return flowOf(Result.success(User("id", UserRole.CUSTOMER, "$firstName $lastName", email, true, null, null)))
    }

    override fun signInWithEmail(email: String, password: String): Flow<Result<User>> {
        return flowOf(Result.success(User("id", UserRole.CUSTOMER, "John Doe", email, true, null, null)))
    }

    override fun sendPasswordResetEmail(email: String): Flow<Result<Unit>> = flowOf(Result.success(Unit))

    override fun confirmPasswordReset(code: String, newPassword: String): Flow<Result<Unit>> = flowOf(Result.success(Unit))

    override fun resendVerificationEmail(email: String?, password: String?): Flow<Result<Unit>> = flowOf(Result.success(Unit))

    override fun checkEmailVerified(): Flow<Result<Boolean>> = flowOf(Result.success(true))

    override fun saveUserToRemote(user: UserProfile): Flow<Result<Unit>> = flowOf(Result.success(Unit))

    override fun getLottieConfig(): Flow<Result<Pair<String, String>>> = flowOf(Result.success("" to ""))

    override fun getSplashTexts(locale: String): Flow<Result<Pair<String, String>>> = flowOf(Result.success("" to ""))

    override fun getCurrentUserId(): String? = currentUserId
}

private class FakeSubscriptionRepository : SubscriptionRepository {
    private val status = SubscriptionStatus(SubscriptionTier.FREE, isActive = false)

    override fun observeSubscriptionStatus(): Flow<SubscriptionStatus> = flowOf(status)

    override suspend fun getSubscriptionStatus(): Result<SubscriptionStatus> = Result.success(status)

    override suspend fun startSubscriptionPurchase(productId: String): Result<Unit> = Result.success(Unit)

    override suspend fun refreshEntitlements(): Result<SubscriptionStatus> = Result.success(status)
}
