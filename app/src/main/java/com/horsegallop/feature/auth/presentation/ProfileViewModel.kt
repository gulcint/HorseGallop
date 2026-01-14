package com.horsegallop.feature.auth.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.auth.model.UserProfile
import com.horsegallop.domain.auth.usecase.GetCurrentUserIdUseCase
import com.horsegallop.domain.auth.usecase.GetUserProfileUseCase
import com.horsegallop.domain.auth.usecase.SignOutUseCase
import com.horsegallop.domain.auth.usecase.UpdateProfileImageUseCase
import com.horsegallop.domain.auth.usecase.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile = UserProfile(),
    val draftProfile: UserProfile = UserProfile(),
    val error: String? = null,
    val isEditing: Boolean = false,
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
                        draftProfile = profile
                    )
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.localizedMessage
                    )
                }
            }
        }
    }

    fun toggleEdit() {
        val current = _uiState.value
        if (current.isEditing) {
            // Cancel editing: revert draft to current profile
            _uiState.value = current.copy(isEditing = false, draftProfile = current.userProfile)
        } else {
            // Start editing: sync draft with current profile (already synced but to be sure)
            _uiState.value = current.copy(isEditing = true, draftProfile = current.userProfile)
        }
    }

    fun updateDraft(
        firstName: String? = null,
        lastName: String? = null,
        phone: String? = null,
        city: String? = null,
        birthDate: String? = null,
        countryCode: String? = null
    ) {
        val currentDraft = _uiState.value.draftProfile
        _uiState.value = _uiState.value.copy(
            draftProfile = currentDraft.copy(
                firstName = firstName ?: currentDraft.firstName,
                lastName = lastName ?: currentDraft.lastName,
                phone = phone ?: currentDraft.phone,
                city = city ?: currentDraft.city,
                birthDate = birthDate ?: currentDraft.birthDate,
                countryCode = countryCode ?: currentDraft.countryCode
            )
        )
    }

    fun saveProfile() {
        val uid = getCurrentUserIdUseCase() ?: return
        val draft = _uiState.value.draftProfile
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            updateUserProfileUseCase(uid, draft).collect { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEditing = false,
                        userProfile = draft
                    )
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
                }
            }
        }
    }

    fun updateProfileImage(uri: Uri) {
        val uid = getCurrentUserIdUseCase() ?: return
        viewModelScope.launch {
            updateProfileImageUseCase(uid, uri).collect { result ->
                result.onSuccess { url ->
                    _uiState.value = _uiState.value.copy(
                        userProfile = _uiState.value.userProfile.copy(photoUrl = url)
                    )
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.localizedMessage)
                }
            }
        }
    }


    fun signOut(onSignOut: () -> Unit) {
        viewModelScope.launch {
            signOutUseCase.execute().collect {
                onSignOut()
            }
        }
    }
}
