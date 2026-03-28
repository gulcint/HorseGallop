package com.horsegallop.feature.equestrian.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.equestrian.model.EquestrianAnnouncement
import com.horsegallop.domain.equestrian.model.EquestrianCompetition
import com.horsegallop.domain.equestrian.usecase.GetEquestrianAnnouncementsUseCase
import com.horsegallop.domain.equestrian.usecase.GetEquestrianCompetitionsUseCase
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
    COMPETITIONS,
    TBF
}

sealed interface AgendaPreviewItem {
    data class Announcement(val item: EquestrianAnnouncement) : AgendaPreviewItem
    data class Competition(val item: EquestrianCompetition) : AgendaPreviewItem
}

data class EquestrianAgendaUiState(
    val selectedTab: EquestrianAgendaTab = EquestrianAgendaTab.ANNOUNCEMENTS,
    val announcements: List<EquestrianAnnouncement> = emptyList(),
    val competitions: List<EquestrianCompetition> = emptyList(),
    val isLoadingAnnouncements: Boolean = true,
    val isLoadingCompetitions: Boolean = true,
    val announcementsError: String? = null,
    val competitionsError: String? = null,
    val previewItem: AgendaPreviewItem? = null
)

@HiltViewModel
class EquestrianAgendaViewModel @Inject constructor(
    private val getEquestrianAnnouncementsUseCase: GetEquestrianAnnouncementsUseCase,
    private val getEquestrianCompetitionsUseCase: GetEquestrianCompetitionsUseCase,
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
        loadAnnouncements()
        loadCompetitions()
        triggerSync(force = false)
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

    private fun triggerSync(force: Boolean) {
        viewModelScope.launch {
            triggerFederationManualSyncUseCase(force = force)
                .onSuccess { loadAnnouncements() }
                .onFailure { /* silent — data still shown if cached */ }
        }
    }

    private fun loadAnnouncements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAnnouncements = true, announcementsError = null) }
            getEquestrianAnnouncementsUseCase()
                .onSuccess { items ->
                    android.util.Log.d("EquestrianAgenda", "✅ Loaded ${items.size} announcements: ${items.map { it.title }}")
                    _uiState.update { it.copy(isLoadingAnnouncements = false, announcements = items) }
                }
                .onFailure { error ->
                    android.util.Log.e("EquestrianAgenda", "❌ Error loading announcements: ${error.message}", error)
                    _uiState.update { it.copy(isLoadingAnnouncements = false, announcementsError = "Veriler yüklenemedi. Lütfen tekrar deneyin.") }
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
                    _uiState.update { it.copy(isLoadingCompetitions = false, competitionsError = "Veriler yüklenemedi. Lütfen tekrar deneyin.") }
                }
        }
    }
}
