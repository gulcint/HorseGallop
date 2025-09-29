package com.example.core.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.core.R

object LocalizedContent {
  @Composable
  fun getString(@androidx.annotation.StringRes resId: Int): String {
    val context = LocalContext.current
    val provider = DynamicStringProvider(context)
    return provider.getString(resId)
  }
  
  fun getLocalizedTitle(item: com.example.domain.model.SliderItem, isTurkish: Boolean = false): String {
    return if (isTurkish && !item.titleTr.isNullOrBlank()) {
      item.titleTr
    } else {
      item.title
    }
  }
}
