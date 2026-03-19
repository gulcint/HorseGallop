package com.horsegallop.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthTokenInterceptor @Inject constructor(
    private val supabaseClient: SupabaseClient
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val token = try {
            runBlocking { supabaseClient.auth.currentSessionOrNull()?.accessToken }
        } catch (e: Exception) {
            null
        }

        return if (token.isNullOrBlank()) {
            chain.proceed(request)
        } else {
            chain.proceed(
                request.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            )
        }
    }
}
