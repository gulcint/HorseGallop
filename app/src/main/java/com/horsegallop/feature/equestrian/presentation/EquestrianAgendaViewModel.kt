package com.horsegallop.feature.equestrian.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.equestrian.model.EquestrianAnnouncement
import com.horsegallop.domain.equestrian.model.EquestrianCompetition
import com.horsegallop.domain.equestrian.model.FederatedBarnSyncStatus
import com.horsegallop.domain.equestrian.usecase.GetEquestrianAnnouncementsUseCase
import com.horsegallop.domain.equestrian.usecase.GetEquestrianCompetitionsUseCase
import com.horsegallop.domain.equestrian.usecase.GetFederatedBarnSyncStatusUseCase
import com.horsegallop.domain.equestrian.usecase.TriggerFederationManualSyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EquestrianAgendaTab {
    ANNOUNCEMENTS,
    COMPETITIONS
}

enum class SyncActionMessage {
    REFRESHED,
    THROTTLED
}

data class EquestrianAgendaUiState(
    val selectedTab: EquestrianAgendaTab = EquestrianAgendaTab.ANNOUNCEMENTS,
    val announcements: List<EquestrianAnnouncement> = emptyList(),
    val competitions: List<EquestrianCompetition> = emptyList(),
    val syncStatus: FederatedBarnSyncStatus? = null,
    val isLoadingSyncStatus: Boolean = true,
    val isTriggeringSync: Boolean = false,
    val isLoadingAnnouncements: Boolean = true,
    val isLoadingCompetitions: Boolean = true,
    val announcementsError: String? = null,
    val competitionsError: String? = null,
    val syncStatusError: String? = null,
    val syncActionMessage: SyncActionMessage? = null
)

@HiltViewModel
class EquestrianAgendaViewModel @Inject constructor(
    private val getEquestrianAnnouncementsUseCase: GetEquestrianAnnouncementsUseCase,
    private val getEquestrianCompetitionsUseCase: GetEquestrianCompetitionsUseCase,
    private val getFederatedBarnSyncStatusUseCase: GetFederatedBarnSyncStatusUseCase,
    private val triggerFederationManualSyncUseCase: TriggerFederationManualSyncUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EquestrianAgendaUiState())
    val uiState: StateFlow<EquestrianAgendaUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun selectTab(tab: EquestrianAgendaTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun refresh() {
        loadSyncStatus()
        loadAnnouncements()
        loadCompetitions()
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTriggeringSync = true, syncActionMessage = null, syncStatusError = null) }
            triggerFederationManualSyncUseCase()
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isTriggeringSync = false,
                            syncActionMessage = if (result.throttled) {
                                SyncActionMessage.THROTTLED
                            } else {
                                SyncActionMessage.REFRESHED
                            }
                        )
                    }
                    refresh()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isTriggeringSync = false,
                            syncStatusError = error.localizedMessage,
                            syncActionMessage = null
                        )
                    }
                }
        }
    }

    private fun loadSyncStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSyncStatus = true, syncStatusError = null) }
            getFederatedBarnSyncStatusUseCase()
                .onSuccess { status ->
                    _uiState.update {
                        it.copy(
                            isLoadingSyncStatus = false,
                            syncStatus = status
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingSyncStatus = false,
                            syncStatusError = error.localizedMessage
                        )
                    }
                }
        }
    }

    private fun loadAnnouncements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAnnouncements = true, announcementsError = null) }
            getEquestrianAnnouncementsUseCase()
                .onSuccess { items ->
                    _uiState.update { it.copy(isLoadingAnnouncements = false, announcements = items) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoadingAnnouncements = false, announcementsError = error.localizedMessage) }
                }
        }
    }

    private fun loadCompetitions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCompetitions = true, competitionsError = null) }
            getEquestrianCompetitionsUseCase()
                .onSuccess { items ->
                    _uiState.update { it.copy(isLoadingCompetitions = false, competitions = items) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoadingCompetitions = false, competitionsError = error.localizedMessage) }
                }
        }
    }
}
