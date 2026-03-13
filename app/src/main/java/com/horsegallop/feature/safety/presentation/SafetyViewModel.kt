package com.horsegallop.feature.safety.presentation

import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.safety.model.SafetyContact
import com.horsegallop.domain.safety.model.SafetySettings
import com.horsegallop.domain.safety.usecase.AddSafetyContactUseCase
import com.horsegallop.domain.safety.usecase.GetSafetySettingsUseCase
import com.horsegallop.domain.safety.usecase.RemoveSafetyContactUseCase
import com.horsegallop.domain.safety.usecase.TriggerSafetyAlarmUseCase
import com.horsegallop.domain.safety.usecase.UpdateSafetyEnabledUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SafetyUiState(
    val isLoading: Boolean = true,
    val settings: SafetySettings = SafetySettings(),
    val isSaving: Boolean = false,
    val showAddContactSheet: Boolean = false,
    val contactToRemove: SafetyContact? = null,
    val error: String? = null,
    val locationLink: String? = null
)

@HiltViewModel
class SafetyViewModel @Inject constructor(
    private val getSafetySettingsUseCase: GetSafetySettingsUseCase,
    private val updateSafetyEnabledUseCase: UpdateSafetyEnabledUseCase,
    private val addSafetyContactUseCase: AddSafetyContactUseCase,
    private val removeSafetyContactUseCase: RemoveSafetyContactUseCase,
    private val triggerSafetyAlarmUseCase: TriggerSafetyAlarmUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SafetyUiState())
    val uiState: StateFlow<SafetyUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getSafetySettingsUseCase()
                .onSuccess { settings ->
                    _uiState.update { it.copy(isLoading = false, settings = settings) }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.message) }
                }
        }
    }

    fun toggleEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            updateSafetyEnabledUseCase(enabled)
                .onSuccess {
                    _uiState.update { s ->
                        s.copy(
                            isSaving = false,
                            settings = s.settings.copy(isEnabled = enabled)
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isSaving = false, error = err.message) }
                }
        }
    }

    fun addContact(name: String, phone: String) {
        if (name.isBlank() || phone.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, showAddContactSheet = false) }
            addSafetyContactUseCase(name, phone)
                .onSuccess { contact ->
                    _uiState.update { s ->
                        s.copy(
                            isSaving = false,
                            settings = s.settings.copy(contacts = s.settings.contacts + contact)
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isSaving = false, error = err.message) }
                }
        }
    }

    fun removeContact(contact: SafetyContact) {
        _uiState.update { it.copy(contactToRemove = null) }
        viewModelScope.launch {
            removeSafetyContactUseCase(contact.id)
                .onSuccess {
                    _uiState.update { s ->
                        s.copy(
                            settings = s.settings.copy(
                                contacts = s.settings.contacts.filter { it.id != contact.id }
                            )
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(error = err.message) }
                }
        }
    }

    fun showAddContactSheet() {
        _uiState.update { it.copy(showAddContactSheet = true) }
    }

    fun hideAddContactSheet() {
        _uiState.update { it.copy(showAddContactSheet = false) }
    }

    fun promptRemoveContact(contact: SafetyContact) {
        _uiState.update { it.copy(contactToRemove = contact) }
    }

    fun cancelRemoveContact() {
        _uiState.update { it.copy(contactToRemove = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun generateLocationLink() {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val providers = lm?.getProviders(true)
            var bestLoc: Location? = null
            providers?.forEach { provider ->
                @Suppress("MissingPermission")
                val loc = runCatching { lm.getLastKnownLocation(provider) }.getOrNull()
                if (loc != null && (bestLoc == null || loc.accuracy < bestLoc!!.accuracy)) {
                    bestLoc = loc
                }
            }
            _uiState.update {
                it.copy(
                    locationLink = bestLoc?.let { l ->
                        "https://maps.google.com/?q=${l.latitude},${l.longitude}"
                    }
                )
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(locationLink = null) }
        }
    }

    fun clearLocationLink() {
        _uiState.update { it.copy(locationLink = null) }
    }
}
