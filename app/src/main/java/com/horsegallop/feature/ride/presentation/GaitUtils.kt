package com.horsegallop.feature.ride.presentation

import com.google.android.gms.maps.model.LatLng
import com.horsegallop.domain.ride.model.GeoPoint
import com.horsegallop.domain.ride.util.gaitOf

/**
 * GPS nokta listesini yürüyüş tipine (walk/trot/canter) göre ardışık segmentlere böler.
 * Hem RideTrackingScreen hem RideDetailScreen tarafından kullanılır.
 */
fun buildGaitSegments(points: List<GeoPoint>): List<Pair<List<LatLng>, String>> {
    if (points.size < 2) return emptyList()
    val segments = mutableListOf<Pair<MutableList<LatLng>, String>>()
    var currentGait = gaitOf(points.first().speedKmh)
    var current = mutableListOf(LatLng(points.first().latitude, points.first().longitude))
    for (i in 1..points.lastIndex) {
        val pt = points[i]
        val ptGait = gaitOf(pt.speedKmh)
        current.add(LatLng(pt.latitude, pt.longitude))
        if (ptGait != currentGait) {
            if (current.size >= 2) segments.add(current.toMutableList() to currentGait)
            current = mutableListOf(LatLng(pt.latitude, pt.longitude))
            currentGait = ptGait
        }
    }
    if (current.size >= 2) segments.add(current to currentGait)
    return segments
}
