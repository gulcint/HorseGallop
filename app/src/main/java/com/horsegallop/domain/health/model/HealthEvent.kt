package com.horsegallop.domain.health.model

enum class HealthEventType(val defaultIntervalDays: Int) {
    FARRIER(42),      // Nalbant — 6 hafta
    VACCINE(365),     // Aşı — yıllık
    DENTAL(180),      // Diş — 6 ay
    VET(0)            // Veteriner — manuel
}

data class HealthEvent(
    val id: String,
    val userId: String,
    val horseId: String,
    val horseName: String,
    val type: HealthEventType,
    val scheduledDate: Long,      // epoch ms
    val completedDate: Long? = null,
    val notes: String = "",
    val isCompleted: Boolean = false
) {
    val isOverdue: Boolean
        get() = !isCompleted && scheduledDate < System.currentTimeMillis()

    val isDueSoon: Boolean
        get() = !isCompleted && !isOverdue &&
            scheduledDate - System.currentTimeMillis() < 7 * 24 * 60 * 60 * 1000L // 7 gün
}
