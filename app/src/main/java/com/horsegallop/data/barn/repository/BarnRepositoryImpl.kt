package com.horsegallop.data.barn.repository

import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.barn.model.BarnUi
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.barn.repository.BarnRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarnRepositoryImpl @Inject constructor(
    private val functionsDataSource: AppFunctionsDataSource
) : BarnRepository {

    private val cachedBarns = MutableStateFlow<List<BarnWithLocation>>(emptyList())

    override fun getBarns(): Flow<List<BarnWithLocation>> = flow {
        try {
            val remote = functionsDataSource.getBarns().map { dto ->
                BarnWithLocation(
                    barn = BarnUi(
                        id = dto.id,
                        name = dto.name,
                        description = dto.description,
                        location = dto.location,
                        tags = dto.tags,
                        lat = dto.lat,
                        lng = dto.lng,
                        rating = dto.rating,
                        reviewCount = dto.reviewCount,
                        isFavorite = cachedBarns.value.find { it.barn.id == dto.id }?.barn?.isFavorite == true
                    ),
                    lat = dto.lat,
                    lng = dto.lng,
                    amenities = dto.amenities.toSet()
                )
            }
            cachedBarns.value = remote
            emit(remote)
        } catch (_: Exception) {
            emit(cachedBarns.value)
        }
    }

    override fun getBarnById(barnId: String): Flow<BarnWithLocation?> = flow {
        val fromCache = cachedBarns.value.find { it.barn.id == barnId }
        if (fromCache != null) {
            emit(fromCache)
            return@flow
        }

        try {
            val dto = functionsDataSource.getBarnDetail(barnId)
            emit(
                BarnWithLocation(
                    barn = BarnUi(
                        id = dto.id,
                        name = dto.name,
                        description = dto.description,
                        location = dto.location,
                        tags = dto.tags,
                        lat = dto.lat,
                        lng = dto.lng,
                        rating = dto.rating,
                        reviewCount = dto.reviewCount,
                        isFavorite = false
                    ),
                    lat = dto.lat,
                    lng = dto.lng,
                    amenities = dto.amenities.toSet()
                )
            )
        } catch (_: Exception) {
            emit(null)
        }
    }

    override suspend fun toggleFavorite(barnId: String) {
        cachedBarns.update { current ->
            current.map { barnWithLoc ->
                if (barnWithLoc.barn.id == barnId) {
                    barnWithLoc.copy(
                        barn = barnWithLoc.barn.copy(isFavorite = !barnWithLoc.barn.isFavorite)
                    )
                } else {
                    barnWithLoc
                }
            }
        }
    }
}
