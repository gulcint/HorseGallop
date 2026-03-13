package com.horsegallop.data.barn.repository

import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.barn.model.BarnReview
import com.horsegallop.domain.barn.model.BarnUi
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.barn.model.Instructor
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
            emit(fromCache.copy(barn = fromCache.barn.copy(
                instructors = mockInstructors,
                recentReviews = mockReviews
            )))
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
                        heroImageUrl = dto.heroImageUrl,
                        capacity = dto.capacity,
                        phone = dto.phone,
                        isFavorite = false,
                        instructors = mockInstructors,
                        recentReviews = mockReviews
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

    companion object {
        val mockInstructors = listOf(
            Instructor(
                id = "i1",
                name = "Ahmet Yilmaz",
                photoUrl = null,
                specialty = "Dressage",
                rating = 4.9
            ),
            Instructor(
                id = "i2",
                name = "Ayse Kaya",
                photoUrl = null,
                specialty = "Show Jumping",
                rating = 4.8
            ),
            Instructor(
                id = "i3",
                name = "Mehmet Demir",
                photoUrl = null,
                specialty = "Trail Riding",
                rating = 4.7
            )
        )

        val mockReviews = listOf(
            BarnReview(
                id = "r1",
                authorName = "Selin A.",
                rating = 5,
                comment = "Wonderful experience! The instructors are very professional and caring.",
                dateLabel = "March 2026"
            ),
            BarnReview(
                id = "r2",
                authorName = "Kemal T.",
                rating = 4,
                comment = "Great facilities and well-maintained horses. Would highly recommend.",
                dateLabel = "February 2026"
            ),
            BarnReview(
                id = "r3",
                authorName = "Deniz M.",
                rating = 5,
                comment = "Best barn in the area. My kids love coming here every weekend.",
                dateLabel = "January 2026"
            )
        )
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
