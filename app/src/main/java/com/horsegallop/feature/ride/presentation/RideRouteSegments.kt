package com.horsegallop.feature.ride.presentation

import com.google.android.gms.maps.model.LatLng
import com.horsegallop.core.util.haversineKm
import com.horsegallop.domain.ride.model.GeoPoint
import com.horsegallop.domain.ride.util.gaitOf

private const val MIN_SEGMENT_DISTANCE_METERS = 4.0
private const val MAX_POINT_JUMP_METERS = 180.0

data class RideRouteSegment(
    val points: List<LatLng>,
    val gait: String
)

data class RideRoutePresentation(
    val filteredPoints: List<GeoPoint>,
    val segments: List<RideRouteSegment>
)

fun buildRideRoutePresentation(points: List<GeoPoint>): RideRoutePresentation {
    val filteredPoints = filterRideRoutePoints(points)
    if (filteredPoints.size < 2) {
        return RideRoutePresentation(
            filteredPoints = filteredPoints,
            segments = emptyList()
        )
    }

    val segments = mutableListOf<RideRouteSegment>()
    var currentGait = gaitOf(filteredPoints.first().speedKmh)
    var currentPoints = mutableListOf(
        LatLng(filteredPoints.first().latitude, filteredPoints.first().longitude)
    )

    for (i in 1..filteredPoints.lastIndex) {
        val point = filteredPoints[i]
        val gait = gaitOf(point.speedKmh)
        val latLng = LatLng(point.latitude, point.longitude)

        if (gait != currentGait && currentPoints.size >= 2) {
            segments += RideRouteSegment(currentPoints.toList(), currentGait)
            currentPoints = mutableListOf(currentPoints.last(), latLng)
            currentGait = gait
        } else {
            currentPoints.add(latLng)
            currentGait = gait
        }
    }

    if (currentPoints.size >= 2) {
        segments += RideRouteSegment(currentPoints.toList(), currentGait)
    }

    return RideRoutePresentation(
        filteredPoints = filteredPoints,
        segments = segments
    )
}

private fun filterRideRoutePoints(points: List<GeoPoint>): List<GeoPoint> {
    if (points.isEmpty()) return emptyList()

    val cleaned = ArrayList<GeoPoint>(points.size)
    var lastAccepted: GeoPoint? = null

    points.forEach { point ->
        if (point.latitude == 0.0 && point.longitude == 0.0) return@forEach

        val previous = lastAccepted
        if (previous == null) {
            cleaned += point
            lastAccepted = point
            return@forEach
        }

        val distanceMeters = haversineKm(
            previous.latitude,
            previous.longitude,
            point.latitude,
            point.longitude
        ) * 1000.0

        if (distanceMeters < MIN_SEGMENT_DISTANCE_METERS) return@forEach
        if (distanceMeters > MAX_POINT_JUMP_METERS) return@forEach

        cleaned += point
        lastAccepted = point
    }

    if (cleaned.size == 1 && points.size > 1) {
        return listOf(cleaned.first(), points.last())
            .filterNot { it.latitude == 0.0 && it.longitude == 0.0 }
    }

    return cleaned
}
