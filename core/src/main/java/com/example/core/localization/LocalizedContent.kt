package com.example.core.localization

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

object LocalizedContent {
  @Composable
  fun getString(@androidx.annotation.StringRes resId: Int): String {
    val context = LocalContext.current
    return context.getString(resId)
  }
}
