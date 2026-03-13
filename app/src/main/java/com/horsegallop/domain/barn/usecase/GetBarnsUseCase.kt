package com.horsegallop.domain.barn.usecase

import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.barn.repository.BarnRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBarnsUseCase @Inject constructor(
    private val repository: BarnRepository
) {
    operator fun invoke(lat: Double? = null, lng: Double? = null): Flow<List<BarnWithLocation>> {
        return repository.getBarns(lat, lng)
    }
}
