package com.horsegallop.core.localization

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

object LocalizedContent {
  @Composable
  fun getString(@StringRes resId: Int): String {
    val context = LocalContext.current
    return context.getString(resId)
  }
}
