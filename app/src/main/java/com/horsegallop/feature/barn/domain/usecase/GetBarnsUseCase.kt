package com.horsegallop.feature.barn.domain.usecase

import com.horsegallop.feature.barn.domain.model.BarnWithLocation
import com.horsegallop.feature.barn.domain.repository.BarnRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBarnsUseCase @Inject constructor(
    private val repository: BarnRepository
) {
    operator fun invoke(): Flow<List<BarnWithLocation>> {
        return repository.getBarns()
    }
}
