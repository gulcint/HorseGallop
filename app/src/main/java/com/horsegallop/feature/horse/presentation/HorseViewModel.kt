package com.horsegallop.feature.horse.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.functions.FirebaseFunctionsException
import com.horsegallop.domain.horse.model.Horse
import com.horsegallop.domain.horse.model.HorseGender
import com.horsegallop.domain.horse.usecase.AddHorseUseCase
import com.horsegallop.domain.horse.usecase.DeleteHorseUseCase
import com.horsegallop.domain.horse.usecase.GetBreedsUseCase
import com.horsegallop.domain.horse.usecase.GetMyHorsesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HorseViewModel @Inject constructor(
    private val getMyHorsesUseCase: GetMyHorsesUseCase,
    private val addHorseUseCase: AddHorseUseCase,
    private val deleteHorseUseCase: DeleteHorseUseCase,
    private val getBreedsUseCase: GetBreedsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HorseUiState())
    val uiState: StateFlow<HorseUiState> = _uiState.asStateFlow()

    init {
        loadHorses()
        loadBreeds()
    }

    private fun loadHorses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            try {
                getMyHorsesUseCase().collect { horses ->
                    _uiState.value = _uiState.value.copy(loading = false, horses = horses)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, error = e.localizedMessage)
            }
        }
    }

    private fun loadBreeds() {
        val locale = Locale.getDefault().language
        viewModelScope.launch {
            getBreedsUseCase(locale).onSuccess { breeds ->
                _uiState.value = _uiState.value.copy(breeds = breeds)
            }
            // Non-critical: fallback hardcoded list shown if backend fails
        }
    }

    fun addHorse(name: String, breed: String, birthYear: String, color: String, gender: HorseGender, weightKg: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saving = true, saveError = null)
            val horse = Horse(
                id = "",
                name = name.trim(),
                breed = breed.trim(),
                birthYear = birthYear.toIntOrNull() ?: 0,
                color = color.trim(),
                gender = gender,
                weightKg = weightKg.toIntOrNull() ?: 0
            )
            addHorseUseCase(horse)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(saving = false, savedSuccess = true)
                    loadHorses()
                }
                .onFailure { e ->
                    val msg = when {
                        e is FirebaseFunctionsException -> when (e.code) {
                            FirebaseFunctionsException.Code.NOT_FOUND ->
                                "Sunucu fonksiyonu bulunamadı. Lütfen ağ bağlantınızı kontrol edin."
                            FirebaseFunctionsException.Code.UNAUTHENTICATED ->
                                "Oturum süresi dolmuş. Lütfen tekrar giriş yapın."
                            FirebaseFunctionsException.Code.INVALID_ARGUMENT ->
                                "Geçersiz bilgi girildi. Lütfen alanları kontrol edin."
                            FirebaseFunctionsException.Code.INTERNAL ->
                                "Sunucu hatası oluştu. Lütfen tekrar deneyin."
                            else -> "Bağlantı hatası oluştu. Lütfen tekrar deneyin."
                        }
                        else -> e.localizedMessage ?: "At eklenemedi. Lütfen tekrar deneyin."
                    }
                    _uiState.value = _uiState.value.copy(saving = false, saveError = msg)
                }
        }
    }

    fun deleteHorse(horseId: String) {
        viewModelScope.launch {
            deleteHorseUseCase(horseId).onSuccess { loadHorses() }
        }
    }

    fun clearSaveState() {
        _uiState.value = _uiState.value.copy(savedSuccess = false, saveError = null)
    }
}

data class HorseUiState(
    val loading: Boolean = false,
    val horses: List<Horse> = emptyList(),
    val error: String? = null,
    val saving: Boolean = false,
    val savedSuccess: Boolean = false,
    val saveError: String? = null,
    val breeds: List<String> = emptyList()
)
