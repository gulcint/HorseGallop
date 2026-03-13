package com.horsegallop.domain.ride.util

/** Yürüyüş tipi eşik değerleri ve kalori katsayıları. */
object GaitThresholds {
    const val WALK_MAX_KMH = 6f
    const val TROT_MAX_KMH = 13f

    // Binici MET değerleri (kalori = MET × kilo × saat)
    const val WALK_MET = 3.8f
    const val TROT_MET = 5.5f
    const val CANTER_MET = 7.3f

    // At kalorisi tahmini: ort. 500 kg at, kcal/saat
    const val WALK_HORSE_KCAL_HR = 750f
    const val TROT_HORSE_KCAL_HR = 2000f
    const val CANTER_HORSE_KCAL_HR = 3500f
}

fun gaitOf(speedKmh: Float): String = when {
    speedKmh < GaitThresholds.WALK_MAX_KMH -> "walk"
    speedKmh < GaitThresholds.TROT_MAX_KMH -> "trot"
    else                                    -> "canter"
}
