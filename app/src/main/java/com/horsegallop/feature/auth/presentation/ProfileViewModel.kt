package com.horsegallop.feature.auth.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.R
import com.horsegallop.core.feedback.FeedbackErrorMapper
import com.horsegallop.domain.auth.model.UserProfile
import com.horsegallop.domain.auth.usecase.GetCurrentUserIdUseCase
import com.horsegallop.domain.auth.usecase.GetUserProfileUseCase
import com.horsegallop.domain.auth.usecase.SignOutUseCase
import com.horsegallop.domain.auth.usecase.UpdateProfileImageUseCase
import com.horsegallop.domain.auth.usecase.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.Locale
import javax.inject.Inject

data class ProfileFormErrors(
    val firstNameResId: Int? = null,
    val lastNameResId: Int? = null,
    val phoneResId: Int? = null,
    val weightResId: Int? = null
) {
    val hasAnyError: Boolean
        get() = firstNameResId != null || lastNameResId != null || phoneResId != null || weightResId != null
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val userProfile: UserProfile = UserProfile(),
    val draftProfile: UserProfile = UserProfile(),
    val draftWeightInput: String = "",
    val formErrors: ProfileFormErrors = ProfileFormErrors(),
    val errorMessageResId: Int? = null,
    val successMessageResId: Int? = null,
    val isEditSessionActive: Boolean = false,
    val countryCodes: List<String> = listOf("+90", "+1", "+44", "+49", "+33", "+34", "+39", "+61", "+81", "+86", "+971", "+7")
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val updateProfileImageUseCase: UpdateProfileImageUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        val uid = getCurrentUserIdUseCase() ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            getUserProfileUseCase(uid).collect { result ->
                result.onSuccess { profile ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userProfile = profile,
                        draftProfile = profile,
                        draftWeightInput = formatWeightInput(profile.weight),
                        errorMessageResId = null
                    )
                }.onFailure { e ->
                    FeedbackErrorMapper.logTechnicalError("ProfileViewModel.loadProfile", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessageResId = mapProfileErrorRes(e)
                    )
                }
            }
        }
    }

    fun startEditSession(force: Boolean = false) {
        val current = _uiState.value
        if (current.isEditSessionActive && !force) return

        _uiState.value = current.copy(
            isEditSessionActive = true,
            draftProfile = current.userProfile,
            draftWeightInput = formatWeightInput(current.userProfile.weight),
            formErrors = ProfileFormErrors()
        )
    }

    fun discardEditSession() {
        val current = _uiState.value
        _uiState.value = current.copy(
            isEditSessionActive = false,
            draftProfile = current.userProfile,
            draftWeightInput = formatWeightInput(current.userProfile.weight),
            formErrors = ProfileFormErrors()
        )
    }

    fun updateDraft(
        firstName: String? = null,
        lastName: String? = null,
        phone: String? = null,
        city: String? = null,
        birthDate: String? = null,
        countryCode: String? = null,
        weightInput: String? = null
    ) {
        val currentState = _uiState.value
        val currentDraft = currentState.draftProfile

        _uiState.value = currentState.copy(
            draftProfile = currentDraft.copy(
                firstName = firstName ?: currentDraft.firstName,
                lastName = lastName ?: currentDraft.lastName,
                phone = phone ?: currentDraft.phone,
                city = city ?: currentDraft.city,
                birthDate = birthDate ?: currentDraft.birthDate,
                countryCode = countryCode ?: currentDraft.countryCode
            ),
            draftWeightInput = weightInput ?: currentState.draftWeightInput,
            formErrors = currentState.formErrors.copy(
                firstNameResId = if (firstName != null) null else currentState.formErrors.firstNameResId,
                lastNameResId = if (lastName != null) null else currentState.formErrors.lastNameResId,
                phoneResId = if (phone != null) null else currentState.formErrors.phoneResId,
                weightResId = if (weightInput != null) null else currentState.formErrors.weightResId
            )
        )
    }

    fun saveProfile(onSuccess: (() -> Unit)? = null) {
        val uid = getCurrentUserIdUseCase()
        if (uid == null) {
            _uiState.value = _uiState.value.copy(errorMessageResId = R.string.error_user_session_not_found)
            return
        }

        val validation = validateDraft(_uiState.value)
        if (validation.errors.hasAnyError) {
            _uiState.value = _uiState.value.copy(formErrors = validation.errors)
            return
        }

        val state = _uiState.value
        val profileToSave = state.draftProfile.copy(
            firstName = state.draftProfile.firstName.trim(),
            lastName = state.draftProfile.lastName.trim(),
            phone = state.draftProfile.phone.trim(),
            city = state.draftProfile.city.trim(),
            countryCode = state.draftProfile.countryCode.trim(),
            weight = validation.weight
        )

        _uiState.value = state.copy(
            isSaving = true,
            formErrors = ProfileFormErrors()
        )

        viewModelScope.launch {
            try {
                withTimeout(15000L) {
                    updateUserProfileUseCase(uid, profileToSave).collect { result ->
                        result.onSuccess {
                            _uiState.value = _uiState.value.copy(
                                isSaving = false,
                                userProfile = profileToSave,
                                draftProfile = profileToSave,
                                draftWeightInput = formatWeightInput(profileToSave.weight),
                                successMessageResId = R.string.profile_saved_success,
                                isEditSessionActive = false
                            )
                            onSuccess?.invoke()
                        }.onFailure { e ->
                            FeedbackErrorMapper.logTechnicalError("ProfileViewModel.saveProfile", e)
                            _uiState.value = _uiState.value.copy(
                                isSaving = false,
                                errorMessageResId = mapProfileErrorRes(e)
                            )
                        }
                    }
                }
            } catch (_: TimeoutCancellationException) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessageResId = R.string.feedback_request_timed_out
                )
            } catch (e: Exception) {
                FeedbackErrorMapper.logTechnicalError("ProfileViewModel.saveProfile", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessageResId = mapProfileErrorRes(e)
                )
            }
        }
    }

    fun updateProfileImage(uri: Uri) {
        val uid = getCurrentUserIdUseCase() ?: return
        viewModelScope.launch {
            updateProfileImageUseCase(uid, uri).collect { result ->
                result.onSuccess { url ->
                    val currentState = _uiState.value
                    val updatedUser = currentState.userProfile.copy(photoUrl = url)
                    val updatedDraft = currentState.draftProfile.copy(photoUrl = url)
                    _uiState.value = currentState.copy(
                        userProfile = updatedUser,
                        draftProfile = updatedDraft
                    )
                }.onFailure { e ->
                    FeedbackErrorMapper.logTechnicalError("ProfileViewModel.updateProfileImage", e)
                    _uiState.value = _uiState.value.copy(errorMessageResId = mapProfileErrorRes(e))
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessageResId = null, successMessageResId = null)
    }

    fun signOut(onSignOut: () -> Unit) {
        viewModelScope.launch {
            signOutUseCase.execute().collect {
                onSignOut()
            }
        }
    }

    private data class ValidationResult(
        val errors: ProfileFormErrors,
        val weight: Float?
    )

    private fun validateDraft(state: ProfileUiState): ValidationResult {
        var firstNameError: Int? = null
        var lastNameError: Int? = null
        var phoneError: Int? = null
        var weightError: Int? = null

        val firstName = state.draftProfile.firstName.trim()
        val lastName = state.draftProfile.lastName.trim()
        val phone = state.draftProfile.phone.trim()

        if (firstName.isBlank()) {
            firstNameError = R.string.validation_first_name_required
        }

        if (lastName.isBlank()) {
            lastNameError = R.string.validation_last_name_required
        }

        if (phone.isNotBlank() && (!phone.all { it.isDigit() } || phone.length > 15)) {
            phoneError = R.string.validation_phone_invalid
        }

        val parsedWeight = parseWeightInput(state.draftWeightInput)
        if (parsedWeight == null && state.draftWeightInput.isNotBlank()) {
            weightError = R.string.validation_weight_invalid
        }

        return ValidationResult(
            errors = ProfileFormErrors(
                firstNameResId = firstNameError,
                lastNameResId = lastNameError,
                phoneResId = phoneError,
                weightResId = weightError
            ),
            weight = parsedWeight
        )
    }

    private fun parseWeightInput(weightInput: String): Float? {
        if (weightInput.isBlank()) return null
        val value = weightInput.toFloatOrNull() ?: return null
        if (value < 0f || value > 500f) return null
        return value
    }

    private fun formatWeightInput(weight: Float?): String {
        if (weight == null) return ""
        return if (weight % 1f == 0f) {
            weight.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", weight)
        }
    }

    private fun mapProfileErrorRes(throwable: Throwable): Int {
        val raw = (throwable.message ?: throwable.localizedMessage ?: "").lowercase(Locale.US)
        return when {
            raw.isBlank() -> R.string.error_unknown
            raw.contains("not found") || raw.contains("not_found") -> R.string.feedback_profile_service_not_ready
            raw.contains("timeout") || raw.contains("timed out") -> R.string.feedback_request_timed_out
            raw.contains("network") -> R.string.feedback_service_unavailable
            else -> FeedbackErrorMapper.toMessageRes(throwable)
        }
    }
}
