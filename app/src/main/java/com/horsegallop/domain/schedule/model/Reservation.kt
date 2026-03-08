package com.horsegallop.domain.schedule.model

data class Reservation(
    val id: String,
    val lessonId: String,
    val lessonTitle: String,
    val lessonDate: String,
    val instructorName: String,
    val status: ReservationStatus,
    val createdAt: String = ""
)

enum class ReservationStatus(val displayName: String) {
    PENDING("Bekliyor"),
    CONFIRMED("Onaylandı"),
    CANCELLED("İptal Edildi"),
    COMPLETED("Tamamlandı")
}
