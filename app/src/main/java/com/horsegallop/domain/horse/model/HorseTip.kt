package com.horsegallop.domain.horse.model

data class HorseTip(
    val id: String,
    val title: String,
    val body: String,
    /** Raw category key from backend (e.g. "breed", "physiology", "care", "speed", "anatomy", "vision", "behavior") */
    val category: String = ""
) {
    /** Localized display label — never blank, falls back to "Horse Fact" */
    val categoryLabel: String
        get() = when (category.lowercase().trim()) {
            "breed"      -> "Irk"
            "physiology" -> "Fizyoloji"
            "anatomy"    -> "Anatomi"
            "care"       -> "Bakım"
            "speed"      -> "Hız"
            "vision"     -> "Görüş"
            "behavior"   -> "Davranış"
            ""           -> "At Bilgisi"
            else         -> category.replaceFirstChar { it.uppercase() }
        }
}
