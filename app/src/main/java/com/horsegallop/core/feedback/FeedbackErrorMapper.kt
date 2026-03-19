package com.horsegallop.core.feedback

import androidx.annotation.StringRes
import com.horsegallop.R
import com.horsegallop.core.debug.AppLog
import io.github.jan.supabase.exceptions.RestException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Maps exceptions to user-facing string resources.
 * Firebase-specific exceptions removed in Sprint 5 (Supabase migration).
 */
object FeedbackErrorMapper {

    @StringRes
    fun toMessageRes(error: Throwable?): Int {
        if (error == null) return R.string.error_unknown

        return when (error) {
            is SocketTimeoutException -> R.string.feedback_request_timed_out
            is IOException -> R.string.error_network
            is SecurityException -> R.string.error_user_session_not_found
            is RestException -> fromHttpStatusCode(error.statusCode)
            else -> R.string.error_unknown
        }
    }

    @StringRes
    private fun fromHttpStatusCode(statusCode: Int): Int = when (statusCode) {
        401, 403 -> R.string.error_user_session_not_found
        404 -> R.string.feedback_profile_service_not_ready
        503 -> R.string.feedback_service_unavailable
        408 -> R.string.feedback_request_timed_out
        else -> R.string.error_unknown
    }

    fun logTechnicalError(tag: String, error: Throwable?) {
        if (error == null) return
        val details = buildString {
            append(error::class.java.simpleName)
            append(": ")
            append(error.message.orEmpty())
            if (error is RestException) {
                append(" [httpStatus=")
                append(error.statusCode)
                append("]")
            }
        }
        AppLog.e(tag, details)
    }
}
