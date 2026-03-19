package com.horsegallop.feature.health.presentation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.auth.usecase.GetCurrentUserIdUseCase
import com.horsegallop.domain.health.model.HealthEvent
import com.horsegallop.domain.health.usecase.DeleteHealthEventUseCase
import com.horsegallop.domain.health.usecase.GetHealthEventsUseCase
import com.horsegallop.domain.health.usecase.SaveHealthEventUseCase
import com.horsegallop.domain.horse.model.Horse
import com.horsegallop.domain.horse.usecase.GetMyHorsesUseCase
import com.horsegallop.feature.health.notification.HealthReminderReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HealthUiState(
    val loading: Boolean = true,
    val events: List<HealthEvent> = emptyList(),
    val horses: List<Horse> = emptyList(),
    val error: String? = null,
    val selectedHorseId: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val getHealthEventsUseCase: GetHealthEventsUseCase,
    private val saveHealthEventUseCase: SaveHealthEventUseCase,
    private val deleteHealthEventUseCase: DeleteHealthEventUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getMyHorsesUseCase: GetMyHorsesUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(HealthUiState())
    val ui: StateFlow<HealthUiState> = _ui.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
        loadHorses()
    }

    private fun loadHorses() {
        viewModelScope.launch {
            getMyHorsesUseCase().collect { horseList ->
                _ui.update { it.copy(horses = horseList) }
            }
        }
    }

    fun getCurrentUserId(): String? = getCurrentUserIdUseCase()

    fun load() {
        _ui.update { it.copy(loading = true, error = null) }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getHealthEventsUseCase(_ui.value.selectedHorseId)
                .collect { events ->
                    _ui.update { it.copy(loading = false, events = events) }
                }
        }
    }

    fun filterByHorse(horseId: String?) {
        _ui.update { it.copy(selectedHorseId = horseId) }
        load()
    }

    fun saveEvent(event: HealthEvent) {
        _ui.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            saveHealthEventUseCase(event)
                .onSuccess { _ui.update { it.copy(isSaving = false) } }
                .onFailure { e -> _ui.update { it.copy(isSaving = false, error = e.localizedMessage) } }
        }
    }

    fun markCompleted(event: HealthEvent) {
        viewModelScope.launch {
            val updated = event.copy(
                isCompleted = true,
                completedDate = System.currentTimeMillis()
            )
            saveHealthEventUseCase(updated)
                .onFailure { e -> _ui.update { it.copy(error = e.localizedMessage) } }
        }
    }

    fun delete(eventId: String) {
        viewModelScope.launch {
            deleteHealthEventUseCase(eventId)
                .onFailure { e -> _ui.update { it.copy(error = e.localizedMessage) } }
        }
    }

    fun clearError() = _ui.update { it.copy(error = null) }

    /**
     * Schedules a local notification alarm for [event] at [event.scheduledDate] - 24 hours.
     * [notificationTitle] should be pre-built by the Screen layer (using stringResource) since
     * ViewModel cannot access Context string resources safely.
     * Uses inexact AlarmManager.setWindow() — does not require SCHEDULE_EXACT_ALARM permission.
     * Note: alarms do NOT survive device reboot (RECEIVE_BOOT_COMPLETED is out of MVP scope).
     */
    fun scheduleReminder(context: Context, event: HealthEvent, notificationTitle: String) {
        val triggerAt = event.scheduledDate - 24L * 60 * 60 * 1000
        if (triggerAt <= System.currentTimeMillis()) return
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, HealthReminderReceiver::class.java).apply {
            putExtra(HealthReminderReceiver.EXTRA_TITLE, notificationTitle)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // 15-minute window → inexact, no special permission needed
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            15L * 60 * 1000,
            pi
        )
    }

    /**
     * Cancels a previously scheduled reminder for [eventId].
     */
    fun cancelReminder(context: Context, eventId: String) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, HealthReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pi?.let { alarmManager.cancel(it) }
    }
}
