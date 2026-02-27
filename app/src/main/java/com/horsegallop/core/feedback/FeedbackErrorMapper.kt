package com.horsegallop.core.feedback

import androidx.annotation.StringRes
import com.google.firebase.functions.FirebaseFunctionsException
import com.horsegallop.R
import com.horsegallop.core.debug.AppLog
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.Locale

object FeedbackErrorMapper {

    @StringRes
    fun toMessageRes(error: Throwable?): Int {
        if (error == null) return R.string.error_unknown

        val firebaseCodeName = (error as? FirebaseFunctionsException)?.code?.name
        if (!firebaseCodeName.isNullOrBlank()) {
            return fromFirebaseCodeName(firebaseCodeName)
        }

        return when (error) {
            is SocketTimeoutException -> R.string.feedback_request_timed_out
            is IOException -> R.string.error_network
            is SecurityException -> R.string.error_user_session_not_found
            else -> R.string.error_unknown
        }
    }

    @StringRes
    fun fromFirebaseCode(code: FirebaseFunctionsException.Code): Int = fromFirebaseCodeName(code.name)

    @StringRes
    fun fromFirebaseCodeName(codeName: String?): Int = when (codeName?.uppercase(Locale.US)) {
        "NOT_FOUND" -> R.string.feedback_profile_service_not_ready
        "UNAVAILABLE" -> R.string.feedback_service_unavailable
        "DEADLINE_EXCEEDED" -> R.string.feedback_request_timed_out
        "PERMISSION_DENIED",
        "UNAUTHENTICATED" -> R.string.error_user_session_not_found
        else -> R.string.error_unknown
    }

    fun logTechnicalError(tag: String, error: Throwable?) {
        if (error == null) return
        val details = buildString {
            append(error::class.java.simpleName)
            append(": ")
            append(error.message.orEmpty())
            val firebaseCode = (error as? FirebaseFunctionsException)?.code
            if (firebaseCode != null) {
                append(" [firebaseCode=")
                append(firebaseCode.name)
                append("]")
            }
        }
        AppLog.e(tag, details)
    }
}
