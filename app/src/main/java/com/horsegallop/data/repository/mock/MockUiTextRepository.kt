package com.horsegallop.data.repository.mock

import com.horsegallop.data.uitext.FakeUiTextData
import com.horsegallop.domain.uitext.LocaleCode
import com.horsegallop.domain.uitext.ScreenId
import com.horsegallop.domain.uitext.UiText
import com.horsegallop.domain.uitext.UiTextKey
import com.horsegallop.domain.uitext.UiTextRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class MockUiTextRepository : UiTextRepository {
  private val textsFlow: MutableStateFlow<List<UiText>> = MutableStateFlow(FakeUiTextData.all)
  override fun getAllForScreen(screenId: ScreenId, locale: LocaleCode): Flow<List<UiText>> {
    return textsFlow.map { list -> list.filter { it.key.screenId == screenId && it.locale == locale } }
  }
  override fun getByKey(key: UiTextKey, locale: LocaleCode): Flow<UiText?> {
    return textsFlow.map { list -> list.firstOrNull { it.key == key && it.locale == locale } }
  }
}


