package com.horsegallop.feature.equestrian.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.equestrian.model.EquestrianAnnouncement
import com.horsegallop.domain.equestrian.model.EquestrianCompetition
import com.horsegallop.domain.equestrian.model.FederationSourceHealthItem
import com.horsegallop.domain.equestrian.model.FederatedBarnSyncStatus
import com.horsegallop.domain.equestrian.usecase.GetEquestrianAnnouncementsUseCase
import com.horsegallop.domain.equestrian.usecase.GetEquestrianCompetitionsUseCase
import com.horsegallop.domain.equestrian.usecase.GetFederationSourceHealthUseCase
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
    THROTTLED,
    DEBUG_REFRESHED
}

sealed interface AgendaPreviewItem {
    data class Announcement(val item: EquestrianAnnouncement) : AgendaPreviewItem
    data class Competition(val item: EquestrianCompetition) : AgendaPreviewItem
}

data class EquestrianAgendaUiState(
    val selectedTab: EquestrianAgendaTab = EquestrianAgendaTab.ANNOUNCEMENTS,
    val announcements: List<EquestrianAnnouncement> = emptyList(),
    val competitions: List<EquestrianCompetition> = emptyList(),
    val syncStatus: FederatedBarnSyncStatus? = null,
    val sourceHealth: List<FederationSourceHealthItem> = emptyList(),
    val isLoadingSyncStatus: Boolean = true,
    val isLoadingSourceHealth: Boolean = true,
    val isTriggeringSync: Boolean = false,
    val isLoadingAnnouncements: Boolean = true,
    val isLoadingCompetitions: Boolean = true,
    val announcementsError: String? = null,
    val competitionsError: String? = null,
    val syncStatusError: String? = null,
    val sourceHealthError: String? = null,
    val syncActionMessage: SyncActionMessage? = null,
    val previewItem: AgendaPreviewItem? = null
)

@HiltViewModel
class EquestrianAgendaViewModel @Inject constructor(
    private val getEquestrianAnnouncementsUseCase: GetEquestrianAnnouncementsUseCase,
    private val getEquestrianCompetitionsUseCase: GetEquestrianCompetitionsUseCase,
    private val getFederationSourceHealthUseCase: GetFederationSourceHealthUseCase,
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
        loadSourceHealth()
        loadAnnouncements()
        loadCompetitions()
    }

    fun showAnnouncementPreview(item: EquestrianAnnouncement) {
        _uiState.update { it.copy(previewItem = AgendaPreviewItem.Announcement(item)) }
    }

    fun showCompetitionPreview(item: EquestrianCompetition) {
        _uiState.update { it.copy(previewItem = AgendaPreviewItem.Competition(item)) }
    }

    fun dismissPreview() {
        _uiState.update { it.copy(previewItem = null) }
    }

    fun triggerManualSync() {
        triggerSync(force = false)
    }

    fun triggerDebugSync() {
        triggerSync(force = true)
    }

    private fun triggerSync(force: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTriggeringSync = true, syncActionMessage = null, syncStatusError = null) }
            triggerFederationManualSyncUseCase(force = force)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isTriggeringSync = false,
                            syncActionMessage = if (force) {
                                SyncActionMessage.DEBUG_REFRESHED
                            } else {
                                if (result.throttled) {
                                    SyncActionMessage.THROTTLED
                                } else {
                                    SyncActionMessage.REFRESHED
                                }
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

    private fun loadSourceHealth() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSourceHealth = true, sourceHealthError = null) }
            getFederationSourceHealthUseCase()
                .onSuccess { items ->
                    _uiState.update {
                        it.copy(
                            isLoadingSourceHealth = false,
                            sourceHealth = items
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingSourceHealth = false,
                            sourceHealthError = error.localizedMessage
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
