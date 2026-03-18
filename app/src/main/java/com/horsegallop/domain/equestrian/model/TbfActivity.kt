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
        val monthTr = startDate.month.turkishName()
        return if (isMultiDay)
            "${startDate.dayOfMonth}-${endDate.dayOfMonth} $monthTr ${startDate.year}"
        else
            "${startDate.dayOfMonth} $monthTr ${startDate.year}"
    }
}

enum class TbfDiscipline(val displayNameTr: String) {
    SHOW_JUMPING("Engel Atlama"),
    ENDURANCE("Atli Dayaniklilik"),
    DRESSAGE("At Terbiyesi"),
    PONY("Pony"),
    VAULTING("Atli Cimnastik"),
    EVENTING("Uc Gunluk"),
    OTHER("Diger");

    companion object {
        fun fromString(value: String): TbfDiscipline =
            entries.firstOrNull { it.name.lowercase() == value.lowercase().replace(" ", "_") } ?: OTHER
    }
}

enum class TbfActivityType(val displayNameTr: String) {
    INTERNATIONAL("Uluslararasi"),
    CHAMPIONSHIP("Sampiyona"),
    CUP("Kupa"),
    INCENTIVE("Tesvik"),
    EDUCATION("Egitim"),
    CATEGORY_EXAM("Kategori Sinavi"),
    SEMINAR("Seminer"),
    CONFERENCE("Konferans"),
    WORKSHOP("Calistay"),
    OTHER("Diger");

    companion object {
        fun fromString(value: String): TbfActivityType =
            entries.firstOrNull { it.name.lowercase() == value.lowercase() } ?: OTHER
    }
}

fun Month.turkishName(): String = when (this) {
    Month.JANUARY -> "Ocak"
    Month.FEBRUARY -> "Subat"
    Month.MARCH -> "Mart"
    Month.APRIL -> "Nisan"
    Month.MAY -> "Mayis"
    Month.JUNE -> "Haziran"
    Month.JULY -> "Temmuz"
    Month.AUGUST -> "Agustos"
    Month.SEPTEMBER -> "Eylul"
    Month.OCTOBER -> "Ekim"
    Month.NOVEMBER -> "Kasim"
    Month.DECEMBER -> "Aralik"
}
