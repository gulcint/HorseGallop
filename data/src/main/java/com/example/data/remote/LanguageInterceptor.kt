package com.example.data.remote

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale

class LanguageInterceptor(private val context: Context) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()
    val locale = Locale.getDefault()
    val languageCode = locale.language // "tr", "en", etc.
    
    val requestWithLanguage = originalRequest.newBuilder()
      .header("Accept-Language", languageCode)
      .build()
    
    return chain.proceed(requestWithLanguage)
  }
}
