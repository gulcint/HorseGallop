package com.horsegallop.feature.auth.presentation

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun profileScreen_editButton_triggersEditNavigationCallback() {
        val viewModel = buildViewModel().first
        var editClicked = false

        composeRule.setContent {
            ProfileScreen(
                onBack = {},
                onSettings = {},
                onLogout = {},
                onEditProfile = { editClicked = true },
                viewModel = viewModel
            )
        }

        composeRule.onNodeWithTag(ProfileTestTags.EditButton).performClick()
        assertTrue(editClicked)
    }

    @Test
    fun editProfileScreen_invalidForm_showsValidationMessage() {
        val viewModel = buildViewModel().first

        composeRule.setContent {
            EditProfileScreen(
                onBack = {},
                viewModel = viewModel
            )
        }

        composeRule.onNodeWithTag(ProfileTestTags.FirstNameField).performTextClearance()
        composeRule.onNodeWithTag(ProfileTestTags.SaveButton).performClick()

        composeRule.onNodeWithText("First name is required").assertIsDisplayed()
    }

    @Test
    fun editProfileScreen_validForm_callsOnBackAfterSave() {
        val viewModel = buildViewModel().first
        var onBackCalled = false

        composeRule.setContent {
            EditProfileScreen(
                onBack = { onBackCalled = true },
                viewModel = viewModel
            )
        }

        composeRule.onNodeWithTag(ProfileTestTags.SaveButton).performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) { onBackCalled }
        assertTrue(onBackCalled)
    }

    @Test
    fun profileScreen_showsWeightInSnapshot() {
        val seeded = UserProfile(
            firstName = "Ada",
            lastName = "Lovelace",
            email = "ada@example.com",
            phone = "5551234567",
            city = "Istanbul",
            birthDate = "1990-01-01",
            countryCode = "+90",
            weight = 72.5f
        )
        val viewModel = buildViewModel(seeded).first

        composeRule.setContent {
            ProfileScreen(
                onBack = {},
                onSettings = {},
                onLogout = {},
                onEditProfile = {},
                viewModel = viewModel
            )
        }

        composeRule.onNodeWithText("72.5 kg").assertIsDisplayed()
    }

    private fun buildViewModel(seedProfile: UserProfile = defaultProfile()): Pair<ProfileViewModel, FakeProfileRepositoryAndroid> {
        val authRepository = FakeAuthRepositoryAndroid("uid-android")
        val profileRepository = FakeProfileRepositoryAndroid(seedProfile)

        val viewModel = ProfileViewModel(
            getCurrentUserIdUseCase = GetCurrentUserIdUseCase(authRepository),
            getUserProfileUseCase = GetUserProfileUseCase(profileRepository),
            updateUserProfileUseCase = UpdateUserProfileUseCase(profileRepository),
            updateProfileImageUseCase = UpdateProfileImageUseCase(profileRepository),
            signOutUseCase = SignOutUseCase(authRepository)
        )

        return viewModel to profileRepository
    }

    private fun defaultProfile() = UserProfile(
        firstName = "John",
        lastName = "Doe",
        email = "john@example.com",
        phone = "5551234567",
        city = "Istanbul",
        birthDate = "1992-10-10",
        countryCode = "+90",
        weight = 70f
    )
}

private class FakeProfileRepositoryAndroid(
    profile: UserProfile
) : ProfileRepository {

    var currentProfile: UserProfile = profile

    override fun getUserProfile(uid: String): Flow<Result<UserProfile>> {
        return flowOf(Result.success(currentProfile))
    }

    override fun updateUserProfile(uid: String, profile: UserProfile): Flow<Result<Unit>> = flow {
        currentProfile = profile
        emit(Result.success(Unit))
    }

    override fun updateProfileImage(uid: String, uri: Uri): Flow<Result<String>> = flowOf(Result.success("https://example.com/photo.jpg"))

    override fun deleteAccount(): Flow<Result<Unit>> = flowOf(Result.success(Unit))

    override fun signOut() = Unit
}

private class FakeAuthRepositoryAndroid(
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
