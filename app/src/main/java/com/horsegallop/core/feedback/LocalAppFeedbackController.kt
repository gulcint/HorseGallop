package com.horsegallop.core.feedback

import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppFeedbackController = staticCompositionLocalOf<AppFeedbackController> {
    object : AppFeedbackController {
        override suspend fun show(message: AppFeedbackMessage): SnackbarResult = SnackbarResult.Dismissed
    }
}
