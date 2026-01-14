package com.horsegallop.domain.barn.model

import com.horsegallop.domain.barn.model.BarnUi // Need to ensure BarnUi is accessible or move it

data class BarnWithLocation(
    val barn: BarnUi,
    val lat: Double,
    val lng: Double,
    val amenities: Set<String>
)
