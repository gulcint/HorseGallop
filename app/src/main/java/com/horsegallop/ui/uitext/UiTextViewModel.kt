package com.horsegallop.ui.uitext

import androidx.lifecycle.ViewModel
import com.horsegallop.domain.uitext.LocaleCode
import com.horsegallop.domain.uitext.ScreenId
import com.horsegallop.domain.uitext.UiText
import com.horsegallop.domain.uitext.UiTextKey
import com.horsegallop.domain.uitext.UiTextRepository
import kotlinx.coroutines.flow.Flow

class UiTextViewModel(
  private val repository: UiTextRepository
) : ViewModel() {
  fun observeScreenTexts(screenId: ScreenId, locale: LocaleCode): Flow<List<UiText>> {
    return repository.getAllForScreen(screenId, locale)
  }
  fun observeText(key: UiTextKey, locale: LocaleCode): Flow<UiText?> {
    return repository.getByKey(key, locale)
  }
}


