package com.horsegallop.data.barn.repository

import com.horsegallop.core.util.haversineKm
import com.horsegallop.data.remote.supabase.SupabaseDataSource
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
    private val supabaseDataSource: SupabaseDataSource
) : BarnRepository {

    private val cachedBarns = MutableStateFlow<List<BarnWithLocation>>(emptyList())

    override fun getBarns(lat: Double?, lng: Double?): Flow<List<BarnWithLocation>> = flow {
        try {
            val remote = supabaseDataSource.getBarns().map { dto ->
                val distKm = if (lat != null && lng != null && dto.lat != null && dto.lng != null &&
                    dto.lat != 0.0 && dto.lng != 0.0
                ) {
                    haversineKm(lat, lng, dto.lat, dto.lng)
                } else {
                    Double.MAX_VALUE
                }
                BarnWithLocation(
                    barn = BarnUi(
                        id = dto.id,
                        name = dto.name,
                        description = dto.description,
                        location = dto.location,
                        tags = dto.tags,
                        lat = dto.lat ?: 0.0,
                        lng = dto.lng ?: 0.0,
                        rating = dto.rating,
                        reviewCount = dto.reviewCount,
                        heroImageUrl = dto.heroImageUrl,
                        phone = dto.phone,
                        isFavorite = cachedBarns.value.find { it.barn.id == dto.id }?.barn?.isFavorite == true
                    ),
                    lat = dto.lat ?: 0.0,
                    lng = dto.lng ?: 0.0,
                    amenities = dto.amenities.toSet(),
                    distanceKm = distKm
                )
            }.let { barns ->
                if (lat != null && lng != null) {
                    barns.sortedBy { it.distanceKm }
                } else {
                    barns
                }
            }
            cachedBarns.value = remote
            emit(remote)
        } catch (error: Exception) {
            if (cachedBarns.value.isNotEmpty()) {
                emit(cachedBarns.value)
            } else {
                emit(emptyList())
            }
        }
    }

    override fun getBarnById(barnId: String): Flow<BarnWithLocation?> = flow {
        val fromCache = cachedBarns.value.find { it.barn.id == barnId }
        if (fromCache != null) emit(fromCache)

        try {
            val dto = supabaseDataSource.getBarnDetail(barnId)
            if (dto == null) {
                if (fromCache == null) emit(null)
                return@flow
            }
            val instructors = supabaseDataSource.getBarnInstructors(barnId).map { i ->
                Instructor(
                    id = i.id,
                    name = i.name,
                    photoUrl = i.photoUrl.ifBlank { null },
                    specialty = i.specialty,
                    rating = i.rating
                )
            }
            val reviews = supabaseDataSource.getBarnReviews(barnId).map { r ->
                BarnReview(
                    id = r.id,
                    authorName = r.authorName,
                    rating = r.rating,
                    comment = r.comment,
                    dateLabel = r.dateLabel
                )
            }
            emit(
                BarnWithLocation(
                    barn = BarnUi(
                        id = dto.id,
                        name = dto.name,
                        description = dto.description,
                        location = dto.location,
                        tags = dto.tags,
                        lat = dto.lat ?: 0.0,
                        lng = dto.lng ?: 0.0,
                        rating = dto.rating,
                        reviewCount = dto.reviewCount,
                        heroImageUrl = dto.heroImageUrl,
                        capacity = dto.capacity,
                        phone = dto.phone,
                        isFavorite = fromCache?.barn?.isFavorite ?: false,
                        instructors = instructors,
                        recentReviews = reviews
                    ),
                    lat = dto.lat ?: 0.0,
                    lng = dto.lng ?: 0.0,
                    amenities = dto.amenities.toSet()
                )
            )
        } catch (_: Exception) {
            if (fromCache == null) emit(null)
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
