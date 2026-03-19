package com.horsegallop.data.barn.repository

import com.horsegallop.data.remote.supabase.SupabaseBarnDto
import com.horsegallop.data.remote.supabase.SupabaseDataSource
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class BarnRepositoryImplTest {

    private val dataSource: SupabaseDataSource = mock()
    private lateinit var repository: BarnRepositoryImpl

    @Before
    fun setUp() {
        repository = BarnRepositoryImpl(dataSource)
    }

    // ─── getBarns ────────────────────────────────────────────────────────────

    @Test
    fun `getBarns emits mapped barns without coords`() = runTest {
        whenever(dataSource.getBarns()).thenReturn(
            listOf(barnDto("b1", "Ahır 1", lat = 0.0, lng = 0.0))
        )

        val result = repository.getBarns(null, null).toList().first()

        assertEquals(1, result.size)
        assertEquals("b1", result[0].barn.id)
        assertEquals("Ahır 1", result[0].barn.name)
    }

    @Test
    fun `getBarns sorts by distance when lat and lng are provided`() = runTest {
        // b2 is closer to (39.0, 32.0) than b1
        whenever(dataSource.getBarns()).thenReturn(
            listOf(
                barnDto("b1", "Uzak Ahır", lat = 41.0, lng = 29.0),  // ~380 km away
                barnDto("b2", "Yakın Ahır", lat = 39.1, lng = 32.1)   // ~13 km away
            )
        )

        val result = repository.getBarns(39.0, 32.0).toList().first()

        assertEquals("b2", result[0].barn.id)
        assertEquals("b1", result[1].barn.id)
    }

    @Test
    fun `getBarns emits empty list when dataSource throws and cache is empty`() = runTest {
        whenever(dataSource.getBarns()).thenThrow(RuntimeException("network error"))

        val result = repository.getBarns(null, null).toList().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getBarns falls back to cached barns when dataSource throws`() = runTest {
        // First call succeeds — populates cache
        whenever(dataSource.getBarns()).thenReturn(
            listOf(barnDto("b1", "Ahır 1", 0.0, 0.0))
        )
        repository.getBarns(null, null).toList()

        // Second call fails — should return cache
        whenever(dataSource.getBarns()).thenThrow(RuntimeException("network error"))

        val result = repository.getBarns(null, null).toList().first()

        assertEquals(1, result.size)
        assertEquals("b1", result[0].barn.id)
    }

    @Test
    fun `getBarns sets distanceKm to MAX when lat or lng is null`() = runTest {
        whenever(dataSource.getBarns()).thenReturn(
            listOf(barnDto("b1", "Ahır", lat = 39.0, lng = 32.0))
        )

        val result = repository.getBarns(null, null).toList().first()

        assertEquals(Double.MAX_VALUE, result[0].distanceKm, 0.0)
    }

    @Test
    fun `getBarns sets distanceKm to MAX when barn lat or lng is zero`() = runTest {
        whenever(dataSource.getBarns()).thenReturn(
            listOf(barnDto("b1", "Ahır", lat = 0.0, lng = 0.0))
        )

        val result = repository.getBarns(39.0, 32.0).toList().first()

        assertEquals(Double.MAX_VALUE, result[0].distanceKm, 0.0)
    }

    // ─── getBarnById ─────────────────────────────────────────────────────────

    @Test
    fun `getBarnById emits null when cache is empty and dataSource throws`() = runTest {
        whenever(dataSource.getBarnDetail("b1")).thenThrow(RuntimeException("not found"))
        whenever(dataSource.getBarnInstructors("b1")).thenReturn(emptyList())
        whenever(dataSource.getBarnReviews("b1")).thenReturn(emptyList())

        val result = repository.getBarnById("b1").toList()

        assertTrue(result.contains(null))
    }

    @Test
    fun `getBarnById emits detail data from dataSource`() = runTest {
        whenever(dataSource.getBarnDetail("b1")).thenReturn(
            barnDto("b1", "Detay Ahır", lat = 39.0, lng = 32.0)
        )
        whenever(dataSource.getBarnInstructors("b1")).thenReturn(emptyList())
        whenever(dataSource.getBarnReviews("b1")).thenReturn(emptyList())

        val emissions = repository.getBarnById("b1").toList()

        val detail = emissions.filterNotNull().last()
        assertEquals("b1", detail.barn.id)
        assertEquals("Detay Ahır", detail.barn.name)
    }

    // ─── toggleFavorite ──────────────────────────────────────────────────────

    @Test
    fun `toggleFavorite sets isFavorite to true when previously false`() = runTest {
        whenever(dataSource.getBarns()).thenReturn(
            listOf(barnDto("b1", "Ahır", 0.0, 0.0))
        )
        // Populate cache
        repository.getBarns(null, null).toList()

        repository.toggleFavorite("b1")

        whenever(dataSource.getBarns()).thenThrow(RuntimeException("fail"))
        val result = repository.getBarns(null, null).toList().first()
        assertTrue(result[0].barn.isFavorite)
    }

    @Test
    fun `toggleFavorite does not affect other barns in cache`() = runTest {
        whenever(dataSource.getBarns()).thenReturn(
            listOf(
                barnDto("b1", "Ahır 1", 0.0, 0.0),
                barnDto("b2", "Ahır 2", 0.0, 0.0)
            )
        )
        repository.getBarns(null, null).toList()

        repository.toggleFavorite("b1")

        whenever(dataSource.getBarns()).thenThrow(RuntimeException("fail"))
        val result = repository.getBarns(null, null).toList().first()
        assertTrue(result.find { it.barn.id == "b1" }!!.barn.isFavorite)
        assertFalse(result.find { it.barn.id == "b2" }!!.barn.isFavorite)
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private fun barnDto(id: String, name: String, lat: Double, lng: Double) = SupabaseBarnDto(
        id = id, name = name, description = "Açıklama", location = "Ankara",
        lat = lat, lng = lng, tags = emptyList(), amenities = emptyList(),
        rating = 4.5, reviewCount = 10
    )
}
