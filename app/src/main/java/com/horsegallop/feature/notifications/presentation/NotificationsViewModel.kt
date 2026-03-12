package com.horsegallop.feature.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.notification.model.AppNotification
import com.horsegallop.domain.notification.usecase.GetNotificationsUseCase
import com.horsegallop.domain.notification.usecase.MarkAllNotificationsReadUseCase
import com.horsegallop.domain.notification.usecase.MarkNotificationReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotifications: GetNotificationsUseCase,
    private val markNotificationRead: MarkNotificationReadUseCase,
    private val markAllNotificationsRead: MarkAllNotificationsReadUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(NotificationsUiState())
    val ui: StateFlow<NotificationsUiState> = _ui

    init {
        getNotifications()
            .onEach { list ->
                _ui.update {
                    it.copy(
                        notifications = list,
                        unreadCount = list.count { n -> !n.isRead },
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onNotificationTap(id: String) {
        viewModelScope.launch {
            markNotificationRead(id)
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            markAllNotificationsRead()
        }
    }
}
