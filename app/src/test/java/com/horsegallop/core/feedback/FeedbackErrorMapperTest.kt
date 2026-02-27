package com.horsegallop.core.feedback

import com.horsegallop.R
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedbackErrorMapperTest {

    @Test
    fun firebaseNotFound_mapsToProfileServiceNotReady() {
        val resId = FeedbackErrorMapper.fromFirebaseCodeName("NOT_FOUND")

        assertEquals(R.string.feedback_profile_service_not_ready, resId)
    }

    @Test
    fun firebaseUnavailable_mapsToServiceUnavailable() {
        val resId = FeedbackErrorMapper.fromFirebaseCodeName("UNAVAILABLE")

        assertEquals(R.string.feedback_service_unavailable, resId)
    }

    @Test
    fun firebaseDeadlineExceeded_mapsToTimeout() {
        val resId = FeedbackErrorMapper.fromFirebaseCodeName("DEADLINE_EXCEEDED")

        assertEquals(R.string.feedback_request_timed_out, resId)
    }

    @Test
    fun genericThrowable_mapsToUnknown() {
        val resId = FeedbackErrorMapper.toMessageRes(IllegalStateException("boom"))

        assertEquals(R.string.error_unknown, resId)
    }
}
