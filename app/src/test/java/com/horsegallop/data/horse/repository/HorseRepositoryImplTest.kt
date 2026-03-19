package com.horsegallop.data.horse.repository

import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseHorseBreedDto
import com.horsegallop.data.remote.supabase.SupabaseHorseDto
import com.horsegallop.data.remote.supabase.SupabaseHorseTipDto
import com.horsegallop.domain.horse.model.Horse
import com.horsegallop.domain.horse.model.HorseGender
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class HorseRepositoryImplTest {

    private val dataSource: SupabaseDataSource = mock()
    private lateinit var repository: HorseRepositoryImpl

    @Before
    fun setUp() {
        repository = HorseRepositoryImpl(dataSource)
    }

    // ─── getMyHorses ─────────────────────────────────────────────────────────

    @Test
    fun `getMyHorses emits mapped domain horses`() = runTest {
        whenever(dataSource.getMyHorses()).thenReturn(
            listOf(horseDto("h1", "Yıldız", "stallion"))
        )

        val result = repository.getMyHorses().first()

        assertEquals(1, result.size)
        assertEquals("Yıldız", result[0].name)
        assertEquals(HorseGender.STALLION, result[0].gender)
    }

    @Test
    fun `getMyHorses emits empty list when dataSource throws`() = runTest {
        whenever(dataSource.getMyHorses()).thenThrow(RuntimeException("network error"))

        val result = repository.getMyHorses().first()

        assertTrue(result.isEmpty())
    }

    // ─── addHorse ────────────────────────────────────────────────────────────

    @Test
    fun `addHorse returns success with mapped domain horse`() = runTest {
        whenever(dataSource.addHorse(any())).thenReturn(horseDto("h2", "Rüzgar", "stallion"))

        val result = repository.addHorse(
            Horse(id = "", name = "Rüzgar", breed = "Arap Atı", birthYear = 2018,
                color = "doru", gender = HorseGender.STALLION, weightKg = 500)
        )

        assertTrue(result.isSuccess)
        assertEquals("Rüzgar", result.getOrNull()?.name)
    }

    @Test
    fun `addHorse returns failure when dataSource throws`() = runTest {
        whenever(dataSource.addHorse(any())).thenThrow(RuntimeException("Add failed"))

        val result = repository.addHorse(
            Horse(id = "", name = "At", breed = "", birthYear = 0,
                color = "", gender = HorseGender.UNKNOWN, weightKg = 0)
        )

        assertTrue(result.isFailure)
    }

    // ─── deleteHorse ─────────────────────────────────────────────────────────

    @Test
    fun `deleteHorse returns success on normal call`() = runTest {
        whenever(dataSource.deleteHorse("h1")).thenReturn(Unit)

        val result = repository.deleteHorse("h1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteHorse returns failure when dataSource throws`() = runTest {
        whenever(dataSource.deleteHorse("h1")).thenThrow(RuntimeException("Delete failed"))

        val result = repository.deleteHorse("h1")

        assertTrue(result.isFailure)
    }

    // ─── getBreeds ───────────────────────────────────────────────────────────

    @Test
    fun `getBreeds returns nameTr when locale is Turkish`() = runTest {
        whenever(dataSource.getBreeds()).thenReturn(
            listOf(SupabaseHorseBreedDto("b1", nameEn = "Arabian", nameTr = "Arap Atı"))
        )

        val result = repository.getBreeds("tr")

        assertTrue(result.isSuccess)
        assertEquals("Arap Atı", result.getOrNull()?.first())
    }

    @Test
    fun `getBreeds returns nameEn when locale is English`() = runTest {
        whenever(dataSource.getBreeds()).thenReturn(
            listOf(SupabaseHorseBreedDto("b1", nameEn = "Arabian", nameTr = "Arap Atı"))
        )

        val result = repository.getBreeds("en")

        assertTrue(result.isSuccess)
        assertEquals("Arabian", result.getOrNull()?.first())
    }

    @Test
    fun `getBreeds falls back to nameEn when nameTr is blank for Turkish locale`() = runTest {
        whenever(dataSource.getBreeds()).thenReturn(
            listOf(SupabaseHorseBreedDto("b1", nameEn = "Arabian", nameTr = ""))
        )

        val result = repository.getBreeds("tr")

        assertEquals("Arabian", result.getOrNull()?.first())
    }

    @Test
    fun `getBreeds returns failure when dataSource throws`() = runTest {
        whenever(dataSource.getBreeds()).thenThrow(RuntimeException("error"))

        val result = repository.getBreeds("en")

        assertTrue(result.isFailure)
    }

    // ─── getHorseTips ────────────────────────────────────────────────────────

    @Test
    fun `getHorseTips returns mapped tips`() = runTest {
        whenever(dataSource.getHorseTips("en")).thenReturn(
            listOf(SupabaseHorseTipDto(id = "t1", title = "Tip", body = "Body", category = "nutrition"))
        )

        val result = repository.getHorseTips("en")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Tip", result.getOrNull()?.first()?.title)
    }

    @Test
    fun `getHorseTips returns failure when dataSource throws`() = runTest {
        whenever(dataSource.getHorseTips(any())).thenThrow(RuntimeException("error"))

        val result = repository.getHorseTips("en")

        assertTrue(result.isFailure)
    }

    // ─── gender mapping (toDomain) ───────────────────────────────────────────

    @Test
    fun `toDomain maps stallion gender correctly`() = runTest {
        whenever(dataSource.getMyHorses()).thenReturn(listOf(horseDto("h1", "At", "stallion")))
        val horse = repository.getMyHorses().first().first()
        assertEquals(HorseGender.STALLION, horse.gender)
    }

    @Test
    fun `toDomain maps mare gender correctly`() = runTest {
        whenever(dataSource.getMyHorses()).thenReturn(listOf(horseDto("h1", "At", "mare")))
        val horse = repository.getMyHorses().first().first()
        assertEquals(HorseGender.MARE, horse.gender)
    }

    @Test
    fun `toDomain maps gelding gender correctly`() = runTest {
        whenever(dataSource.getMyHorses()).thenReturn(listOf(horseDto("h1", "At", "gelding")))
        val horse = repository.getMyHorses().first().first()
        assertEquals(HorseGender.GELDING, horse.gender)
    }

    @Test
    fun `toDomain maps unknown gender for unrecognized string`() = runTest {
        whenever(dataSource.getMyHorses()).thenReturn(listOf(horseDto("h1", "At", "other")))
        val horse = repository.getMyHorses().first().first()
        assertEquals(HorseGender.UNKNOWN, horse.gender)
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private fun horseDto(id: String, name: String, gender: String) = SupabaseHorseDto(
        id = id, name = name, breed = "Arap Atı", birthYear = 2018,
        color = "doru", gender = gender, weightKg = 500
    )
}
