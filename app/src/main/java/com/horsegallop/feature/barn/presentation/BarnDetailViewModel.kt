package com.horsegallop.feature.barn.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.auth.usecase.GetCurrentUserIdUseCase
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.barn.usecase.GetBarnDetailUseCase
import com.horsegallop.domain.schedule.model.Lesson
import com.horsegallop.domain.schedule.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BarnDetailViewModel @Inject constructor(
    private val getBarnDetailUseCase: GetBarnDetailUseCase,
    private val scheduleRepository: ScheduleRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val barnId: String = checkNotNull(savedStateHandle["id"])

    private val currentUserId = getCurrentUserIdUseCase()

    private val _uiState = MutableStateFlow<BarnDetailUiState>(BarnDetailUiState.Loading)
    val uiState: StateFlow<BarnDetailUiState> = _uiState.asStateFlow()

    private val _bookingState = MutableStateFlow(BookingState())
    val bookingState: StateFlow<BookingState> = _bookingState.asStateFlow()

    private val _isOwner = MutableStateFlow(false)
    val isOwner: StateFlow<Boolean> = _isOwner.asStateFlow()

    init {
        loadBarnDetails()
        loadLessons()
    }

    fun refresh() {
        loadBarnDetails()
        loadLessons()
    }

    private fun loadBarnDetails() {
        viewModelScope.launch {
            _uiState.value = BarnDetailUiState.Loading
            getBarnDetailUseCase(barnId).collect { barn ->
                if (barn != null) {
                    _uiState.value = BarnDetailUiState.Success(barn)
                    _isOwner.value = currentUserId != null &&
                        barn.barn.ownerUserId != null &&
                        barn.barn.ownerUserId == currentUserId
                } else {
                    _uiState.value = BarnDetailUiState.Error("Barn not found")
                }
            }
        }
    }

    private fun loadLessons() {
        viewModelScope.launch {
            _bookingState.update { it.copy(isLoadingLessons = true) }
            scheduleRepository.getLessons().collect { lessons ->
                _bookingState.update { it.copy(lessons = lessons, isLoadingLessons = false) }
            }
        }
    }

    fun bookLesson(lessonId: String) {
        viewModelScope.launch {
            _bookingState.update { it.copy(isBooking = true, bookingError = null, bookingSuccess = false) }
            scheduleRepository.bookLesson(lessonId)
                .onSuccess {
                    _bookingState.update { it.copy(isBooking = false, bookingSuccess = true) }
                    // Flow zaten backend güncellemesini dinliyor, ek loadLessons() çağrısı gereksiz
                }
                .onFailure { e ->
                    _bookingState.update { it.copy(isBooking = false, bookingError = "Rezervasyon yapılamadı. Lütfen tekrar deneyin.") }
                }
        }
    }

    fun clearBookingResult() {
        _bookingState.update { it.copy(bookingSuccess = false, bookingError = null) }
    }
}

data class BookingState(
    val lessons: List<Lesson> = emptyList(),
    val isLoadingLessons: Boolean = true,
    val isBooking: Boolean = false,
    val bookingSuccess: Boolean = false,
    val bookingError: String? = null
)

sealed interface BarnDetailUiState {
    data object Loading : BarnDetailUiState
    data class Success(val barn: BarnWithLocation) : BarnDetailUiState
    data class Error(val message: String) : BarnDetailUiState
}
