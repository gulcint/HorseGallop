package com.horsegallop.domain.barn.repository

import com.horsegallop.domain.barn.model.BarnWithLocation
import kotlinx.coroutines.flow.Flow

interface BarnRepository {
    fun getBarns(lat: Double? = null, lng: Double? = null): Flow<List<BarnWithLocation>>
    fun getBarnById(barnId: String): Flow<BarnWithLocation?>
    suspend fun toggleFavorite(barnId: String)
}
