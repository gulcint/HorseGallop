package com.horsegallop.core.feedback

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration

enum class FeedbackTone {
    Success,
    Error,
    Info,
    Warning
}

data class AppFeedbackMessage(
    @StringRes val messageResId: Int,
    val tone: FeedbackTone = FeedbackTone.Info,
    @StringRes val actionLabelResId: Int? = null,
    val withDismissAction: Boolean = false,
    val duration: SnackbarDuration? = null
)
