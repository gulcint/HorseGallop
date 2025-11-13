package com.horsegallop.domain.uitext

import kotlinx.coroutines.flow.Flow

interface UiTextRepository {
  fun getAllForScreen(screenId: ScreenId, locale: LocaleCode): Flow<List<UiText>>
  fun getByKey(key: UiTextKey, locale: LocaleCode): Flow<UiText?>
}


