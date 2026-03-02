package com.horsegallop.domain.content.usecase

import com.horsegallop.domain.content.model.AppContent
import com.horsegallop.domain.content.repository.ContentRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetAppContentUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(locale: String): Flow<Result<AppContent>> = repository.getAppContent(locale)
}
