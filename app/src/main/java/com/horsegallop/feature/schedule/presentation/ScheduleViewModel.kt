package com.horsegallop.feature.schedule.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.schedule.model.Lesson
import com.horsegallop.domain.schedule.model.Reservation
import com.horsegallop.domain.schedule.usecase.BookLessonUseCase
import com.horsegallop.domain.schedule.usecase.CancelReservationUseCase
import com.horsegallop.domain.schedule.usecase.GetLessonsUseCase
import com.horsegallop.domain.schedule.usecase.GetMyReservationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val getLessonsUseCase: GetLessonsUseCase,
    private val bookLessonUseCase: BookLessonUseCase,
    private val cancelReservationUseCase: CancelReservationUseCase,
    private val getMyReservationsUseCase: GetMyReservationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        refresh()
        loadReservations()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                getLessonsUseCase().collect { lessons ->
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        lessons = lessons,
                        isEmpty = lessons.isEmpty()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    lessons = emptyList(),
                    isEmpty = true,
                    error = ERROR_LOAD_LESSONS
                )
            }
        }
    }

    private fun loadReservations() {
        viewModelScope.launch {
            try {
                getMyReservationsUseCase().collect { reservations ->
                    _uiState.value = _uiState.value.copy(reservations = reservations)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    reservations = emptyList(),
                    error = ERROR_LOAD_RESERVATIONS
                )
            }
        }
    }

    fun bookLesson(lessonId: String) {
        _uiState.value = _uiState.value.copy(bookingInProgress = true, bookingError = null)
        viewModelScope.launch {
            bookLessonUseCase(lessonId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        bookingInProgress = false,
                        bookingSuccess = true
                    )
                    refresh()
                    loadReservations()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        bookingInProgress = false,
                        bookingError = ERROR_BOOKING_FAILED
                    )
                }
        }
    }

    fun cancelReservation(reservationId: String) {
        viewModelScope.launch {
            cancelReservationUseCase(reservationId)
                .onSuccess {
                    loadReservations()
                    refresh()
                }
        }
    }

    fun clearBookingState() {
        _uiState.value = _uiState.value.copy(bookingSuccess = false, bookingError = null)
    }
}

private const val ERROR_LOAD_LESSONS = "Dersler yüklenemedi"
private const val ERROR_LOAD_RESERVATIONS = "Rezervasyonlar yüklenemedi"
private const val ERROR_BOOKING_FAILED = "Rezervasyon başarısız"

data class ScheduleUiState(
    val loading: Boolean = true,
    val lessons: List<Lesson> = emptyList(),
    val reservations: List<Reservation> = emptyList(),
    val isEmpty: Boolean = false,
    val error: String? = null,
    val bookingInProgress: Boolean = false,
    val bookingSuccess: Boolean = false,
    val bookingError: String? = null
)
