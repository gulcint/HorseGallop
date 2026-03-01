package com.horsegallop.data.content.repository

import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.content.model.AppContent
import com.horsegallop.domain.content.repository.ContentRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ContentRepositoryImpl @Inject constructor(
    private val functionsDataSource: AppFunctionsDataSource
) : ContentRepository {

    override fun getAppContent(locale: String): Flow<Result<AppContent>> = flow {
        try {
            val content = functionsDataSource.getAppContent(locale)
            emit(
                Result.success(
                    AppContent(
                        locale = content.locale,
                        homeHeroTitle = content.homeHeroTitle,
                        homeHeroSubtitle = content.homeHeroSubtitle,
                        offlineHelp = content.offlineHelp
                    )
                )
            )
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
