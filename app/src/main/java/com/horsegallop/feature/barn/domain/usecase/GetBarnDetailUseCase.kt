package com.horsegallop.feature.barn.domain.usecase

import com.horsegallop.feature.barn.domain.model.BarnWithLocation
import com.horsegallop.feature.barn.domain.repository.BarnRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBarnDetailUseCase @Inject constructor(
    private val barnRepository: BarnRepository
) {
    operator fun invoke(barnId: String): Flow<BarnWithLocation?> {
        return barnRepository.getBarnById(barnId)
    }
}
