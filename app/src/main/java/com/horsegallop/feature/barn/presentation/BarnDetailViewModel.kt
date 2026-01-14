package com.horsegallop.feature.barn.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.barn.usecase.GetBarnDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BarnDetailViewModel @Inject constructor(
    private val getBarnDetailUseCase: GetBarnDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val barnId: String = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow<BarnDetailUiState>(BarnDetailUiState.Loading)
    val uiState: StateFlow<BarnDetailUiState> = _uiState.asStateFlow()

    init {
        loadBarnDetails()
    }

    private fun loadBarnDetails() {
        viewModelScope.launch {
            _uiState.value = BarnDetailUiState.Loading
            getBarnDetailUseCase(barnId).collect { barn ->
                if (barn != null) {
                    _uiState.value = BarnDetailUiState.Success(barn)
                } else {
                    _uiState.value = BarnDetailUiState.Error("Barn not found")
                }
            }
        }
    }
}

sealed interface BarnDetailUiState {
    data object Loading : BarnDetailUiState
    data class Success(val barn: BarnWithLocation) : BarnDetailUiState
    data class Error(val message: String) : BarnDetailUiState
}
