package com.horsegallop.domain.equestrian.model

import java.time.LocalDate
import java.time.Month

data class TbfActivity(
    val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val title: String,
    val organization: String,
    val city: String,
    val discipline: TbfDiscipline,
    val type: TbfActivityType,
    val detailUrl: String = ""
) {
    val isMultiDay: Boolean get() = startDate != endDate

    val dateLabel: String get() {
        return if (!isMultiDay) {
            "${startDate.dayOfMonth} ${startDate.month.turkishName()} ${startDate.year}"
        } else if (startDate.month == endDate.month && startDate.year == endDate.year) {
            "${startDate.dayOfMonth}-${endDate.dayOfMonth} ${startDate.month.turkishName()} ${startDate.year}"
        } else {
            "${startDate.dayOfMonth} ${startDate.month.turkishName()} ${startDate.year} – ${endDate.dayOfMonth} ${endDate.month.turkishName()} ${endDate.year}"
        }
    }
}

enum class TbfDiscipline(val displayNameTr: String) {
    SHOW_JUMPING("Engel Atlama"),
    ENDURANCE("Atlı Dayanıklılık"),
    DRESSAGE("At Terbiyesi"),
    PONY("Pony"),
    VAULTING("Atlı Cimnastik"),
    EVENTING("Üç Günlük"),
    OTHER("Diğer");

    companion object {
        fun fromString(value: String): TbfDiscipline =
            entries.firstOrNull { it.name.lowercase() == value.lowercase().replace(" ", "_") } ?: OTHER
    }
}

enum class TbfActivityType(val displayNameTr: String) {
    INTERNATIONAL("Uluslararası"),
    CHAMPIONSHIP("Şampiyona"),
    CUP("Kupa"),
    INCENTIVE("Teşvik"),
    EDUCATION("Eğitim"),
    CATEGORY_EXAM("Kategori Sınavı"),
    SEMINAR("Seminer"),
    CONFERENCE("Konferans"),
    WORKSHOP("Çalıştay"),
    OTHER("Diğer");

    companion object {
        fun fromString(value: String): TbfActivityType =
            entries.firstOrNull { it.name.lowercase() == value.lowercase() } ?: OTHER
    }
}

fun Month.turkishName(): String = when (this) {
    Month.JANUARY -> "Ocak"
    Month.FEBRUARY -> "Şubat"
    Month.MARCH -> "Mart"
    Month.APRIL -> "Nisan"
    Month.MAY -> "Mayıs"
    Month.JUNE -> "Haziran"
    Month.JULY -> "Temmuz"
    Month.AUGUST -> "Ağustos"
    Month.SEPTEMBER -> "Eylül"
    Month.OCTOBER -> "Ekim"
    Month.NOVEMBER -> "Kasım"
    Month.DECEMBER -> "Aralık"
}
