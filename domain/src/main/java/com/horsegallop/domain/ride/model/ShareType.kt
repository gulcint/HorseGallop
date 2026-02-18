package com.horsegallop.domain.ride.model

enum class ShareType {
    ROUTE_ONLY,           // Sadece rota haritası
    ROUTE_WITH_STATS,     // Rota + istatistikler (mesafe, süre, kalori)
    FULL_RIDE_REPORT      // Tam biniş raporu
}
