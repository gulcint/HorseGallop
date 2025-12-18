package com.horsegallop.feature.barn.domain.repository

import com.horsegallop.feature.barn.domain.model.BarnWithLocation
import kotlinx.coroutines.flow.Flow

interface BarnRepository {
    fun getBarns(): Flow<List<BarnWithLocation>>
    fun getBarnById(barnId: String): Flow<BarnWithLocation?>
}
