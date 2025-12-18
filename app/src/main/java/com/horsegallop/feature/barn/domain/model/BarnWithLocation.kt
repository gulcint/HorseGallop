package com.horsegallop.feature.barn.domain.model

import com.horsegallop.feature.barn.domain.model.BarnUi // Need to ensure BarnUi is accessible or move it

data class BarnWithLocation(
    val barn: BarnUi,
    val lat: Double,
    val lng: Double,
    val amenities: Set<String>
)
