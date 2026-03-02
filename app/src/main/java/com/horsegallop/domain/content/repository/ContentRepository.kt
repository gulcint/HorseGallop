package com.horsegallop.domain.content.repository

import com.horsegallop.domain.content.model.AppContent
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    fun getAppContent(locale: String): Flow<Result<AppContent>>
}
