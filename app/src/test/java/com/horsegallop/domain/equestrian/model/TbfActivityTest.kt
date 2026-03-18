package com.horsegallop.domain.equestrian.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class TbfActivityTest {

    @Test
    fun `TbfDiscipline fromString maps show_jumping correctly`() =
        assertEquals(TbfDiscipline.SHOW_JUMPING, TbfDiscipline.fromString("show_jumping"))

    @Test
    fun `TbfDiscipline fromString returns OTHER for unknown`() =
        assertEquals(TbfDiscipline.OTHER, TbfDiscipline.fromString("bilinmeyen"))

    @Test
    fun `TbfActivityType fromString maps incentive correctly`() =
        assertEquals(TbfActivityType.INCENTIVE, TbfActivityType.fromString("incentive"))

    @Test
    fun `TbfActivity isMultiDay true when dates differ`() {
        val a = TbfActivity(
            "1",
            LocalDate.of(2026, 3, 19),
            LocalDate.of(2026, 3, 22),
            "T",
            "O",
            "C",
            TbfDiscipline.SHOW_JUMPING,
            TbfActivityType.INCENTIVE
        )
        assertTrue(a.isMultiDay)
    }

    @Test
    fun `TbfActivity isMultiDay false when same day`() {
        val a = TbfActivity(
            "2",
            LocalDate.of(2026, 3, 19),
            LocalDate.of(2026, 3, 19),
            "T",
            "O",
            "C",
            TbfDiscipline.SHOW_JUMPING,
            TbfActivityType.INCENTIVE
        )
        assertFalse(a.isMultiDay)
    }

    @Test
    fun `TbfActivity dateLabel format for multi-day`() {
        val a = TbfActivity(
            "3",
            LocalDate.of(2026, 3, 19),
            LocalDate.of(2026, 3, 22),
            "T",
            "O",
            "C",
            TbfDiscipline.SHOW_JUMPING,
            TbfActivityType.INCENTIVE
        )
        assertEquals("19-22 Mart 2026", a.dateLabel)
    }

    @Test
    fun `TbfActivity dateLabel format for single-day`() {
        val a = TbfActivity(
            "4",
            LocalDate.of(2026, 4, 5),
            LocalDate.of(2026, 4, 5),
            "T",
            "O",
            "C",
            TbfDiscipline.DRESSAGE,
            TbfActivityType.CHAMPIONSHIP
        )
        assertEquals("5 Nisan 2026", a.dateLabel)
    }

    @Test
    fun `TbfDiscipline fromString is case-insensitive`() =
        assertEquals(TbfDiscipline.ENDURANCE, TbfDiscipline.fromString("ENDURANCE"))

    @Test
    fun `TbfActivityType fromString maps cup correctly`() =
        assertEquals(TbfActivityType.CUP, TbfActivityType.fromString("cup"))

    @Test
    fun `TbfActivityType fromString returns OTHER for unknown`() =
        assertEquals(TbfActivityType.OTHER, TbfActivityType.fromString("bilinmeyen_tip"))
}
