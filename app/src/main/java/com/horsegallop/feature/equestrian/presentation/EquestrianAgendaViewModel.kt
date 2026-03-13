package com.horsegallop.feature.equestrian.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.equestrian.model.EquestrianAnnouncement
import com.horsegallop.domain.equestrian.model.EquestrianCompetition
import com.horsegallop.domain.equestrian.model.FederatedBarnSyncStatus
import com.horsegallop.domain.equestrian.usecase.GetEquestrianAnnouncementsUseCase
import com.horsegallop.domain.equestrian.usecase.GetEquestrianCompetitionsUseCase
import com.horsegallop.domain.equestrian.usecase.GetFederatedBarnSyncStatusUseCase
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

data class EquestrianAgendaUiState(
    val selectedTab: EquestrianAgendaTab = EquestrianAgendaTab.ANNOUNCEMENTS,
    val announcements: List<EquestrianAnnouncement> = emptyList(),
    val competitions: List<EquestrianCompetition> = emptyList(),
    val syncStatus: FederatedBarnSyncStatus? = null,
    val isLoadingSyncStatus: Boolean = true,
    val isLoadingAnnouncements: Boolean = true,
    val isLoadingCompetitions: Boolean = true,
    val announcementsError: String? = null,
    val competitionsError: String? = null,
    val syncStatusError: String? = null
)

@HiltViewModel
class EquestrianAgendaViewModel @Inject constructor(
    private val getEquestrianAnnouncementsUseCase: GetEquestrianAnnouncementsUseCase,
    private val getEquestrianCompetitionsUseCase: GetEquestrianCompetitionsUseCase,
    private val getFederatedBarnSyncStatusUseCase: GetFederatedBarnSyncStatusUseCase
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
