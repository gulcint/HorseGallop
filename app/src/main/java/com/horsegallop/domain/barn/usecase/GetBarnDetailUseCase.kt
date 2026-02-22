package com.horsegallop.domain.barn.usecase

import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.barn.repository.BarnRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBarnDetailUseCase @Inject constructor(
    private val barnRepository: BarnRepository
) {
    operator fun invoke(barnId: String): Flow<BarnWithLocation?> {
        return barnRepository.getBarnById(barnId)
    }
}
