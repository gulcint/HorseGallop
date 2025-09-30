package com.example.feature_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.SliderItem
import com.example.domain.usecase.GetSliderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
  data object Loading : HomeUiState()
  data class Content(val slides: List<SliderItem>) : HomeUiState()
  data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val getSliderUseCase: GetSliderUseCase
) : ViewModel() {

  private val _uiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState.Loading)
  val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

  fun loadSlider() {
    viewModelScope.launch {
      getSliderUseCase().collect { result ->
        if (result.isSuccess) {
          _uiState.value = HomeUiState.Content(slides = result.getOrThrow())
        } else {
          _uiState.value = HomeUiState.Error(message = result.exceptionOrNull()?.message ?: "Unknown error")
        }
      }
    }
  }
}
