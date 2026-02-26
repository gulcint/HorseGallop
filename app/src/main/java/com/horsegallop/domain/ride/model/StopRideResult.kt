package com.horsegallop.domain.ride.model

data class StopRideResult(
    val localSaved: Boolean,
    val remoteSynced: Boolean,
    val pendingSyncId: String? = null
)
