package com.horsegallop.feature.schedule.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.schedule.model.Lesson
import com.horsegallop.domain.schedule.usecase.GetLessonsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    getLessonsUseCase: GetLessonsUseCase
) : ViewModel() {

    val lessons: StateFlow<List<Lesson>> = getLessonsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
