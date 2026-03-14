package com.horsegallop.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.content.usecase.GetAppContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

data class OnboardingUiState(
    val heroTitle: String? = null,
    val heroSubtitle: String? = null,
    val helpText: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val getAppContentUseCase: GetAppContentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState

    init {
        loadDynamicContent(Locale.getDefault().language)
    }

    private fun loadDynamicContent(locale: String) {
        viewModelScope.launch {
            delay(200)
            getAppContentUseCase(locale).collect { result ->
                result.onSuccess { content ->
                    _uiState.value = _uiState.value.copy(
                        heroTitle = content.onboardingHeroTitle,
                        heroSubtitle = content.onboardingHeroSubtitle,
                        helpText = content.onboardingHelpText
                    )
                }
            }
        }
    }
}
