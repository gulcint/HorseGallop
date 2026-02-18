package com.horsegallop.domain.ride.model

import java.io.Serializable

data class RideShareData(
    val rideSession: RideSession,
    val shareType: ShareType
) : Serializable {

    enum class ShareType {
        ROUTE_ONLY,           // Sadece rota haritası
        ROUTE_WITH_STATS,     // Rota + istatistikler (mesafe, süre, kalori)
        FULL_RIDE_REPORT      // Tam biniş raporu
    }

    companion object {
        fun createRouteOnly(rideSession: RideSession) = 
            RideShareData(rideSession, ShareType.ROUTE_ONLY)
        
        fun createRouteWithStats(rideSession: RideSession) = 
            RideShareData(rideSession, ShareType.ROUTE_WITH_STATS)
        
        fun createFullReport(rideSession: RideSession) = 
            RideShareData(rideSession, ShareType.FULL_RIDE_REPORT)
    }
}
