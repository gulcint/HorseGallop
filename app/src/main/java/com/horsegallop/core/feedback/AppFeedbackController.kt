package com.horsegallop.core.feedback

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

interface AppFeedbackController {
    suspend fun show(message: AppFeedbackMessage): SnackbarResult

    suspend fun showSuccess(@StringRes messageResId: Int): SnackbarResult = show(
        AppFeedbackMessage(
            messageResId = messageResId,
            tone = FeedbackTone.Success
        )
    )

    suspend fun showError(@StringRes messageResId: Int): SnackbarResult = show(
        AppFeedbackMessage(
            messageResId = messageResId,
            tone = FeedbackTone.Error
        )
    )

    suspend fun showInfo(@StringRes messageResId: Int): SnackbarResult = show(
        AppFeedbackMessage(
            messageResId = messageResId,
            tone = FeedbackTone.Info
        )
    )

    suspend fun showWarning(@StringRes messageResId: Int): SnackbarResult = show(
        AppFeedbackMessage(
            messageResId = messageResId,
            tone = FeedbackTone.Warning
        )
    )
}

class SnackbarAppFeedbackController(
    private val hostState: SnackbarHostState,
    private val resources: Resources
) : AppFeedbackController {

    override suspend fun show(message: AppFeedbackMessage): SnackbarResult {
        val visuals = AppSnackbarVisuals(
            message = resources.getString(message.messageResId),
            tone = message.tone,
            actionLabel = message.actionLabelResId?.let(resources::getString),
            withDismissAction = message.withDismissAction,
            duration = message.duration ?: defaultDuration(message.tone)
        )
        return hostState.showSnackbar(visuals)
    }

    private fun defaultDuration(tone: FeedbackTone): SnackbarDuration = when (tone) {
        FeedbackTone.Error -> SnackbarDuration.Long
        FeedbackTone.Warning -> SnackbarDuration.Long
        FeedbackTone.Success -> SnackbarDuration.Short
        FeedbackTone.Info -> SnackbarDuration.Short
    }
}
