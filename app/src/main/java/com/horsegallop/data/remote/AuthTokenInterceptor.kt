package com.horsegallop.data.remote

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthTokenInterceptor @Inject constructor(
    private val auth: FirebaseAuth
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val user = auth.currentUser
        if (user == null) return chain.proceed(request)

        val token = try {
            val task = user.getIdToken(false)
            Tasks.await(task, 5, TimeUnit.SECONDS).token
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
