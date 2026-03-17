package com.horsegallop.data.schedule.repository

import com.horsegallop.data.remote.dto.LessonFunctionsDto
import com.horsegallop.data.remote.dto.ReservationFunctionsDto
import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.schedule.model.ReservationStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ScheduleRepositoryImplTest {

    private val dataSource: AppFunctionsDataSource = mock()
    private lateinit var repository: ScheduleRepositoryImpl

    @Before
    fun setUp() {
        repository = ScheduleRepositoryImpl(dataSource)
    }

    // ─── getLessons ──────────────────────────────────────────────────────────

    @Test
    fun `getLessons emits mapped domain objects when data source succeeds`() = runTest {
        val dto = LessonFunctionsDto(
            id = "lesson1",
            date = "2026-04-01",
            title = "Sabah Dersi",
            instructorName = "Ahmet Hoca",
            durationMin = 60,
            level = "beginner",
            price = 150.0,
            spotsTotal = 5,
            spotsAvailable = 3,
            isBookedByMe = false
        )
        whenever(dataSource.getLessons()).thenReturn(listOf(dto))

        val result = repository.getLessons().first()

        assertEquals(1, result.size)
        val lesson = result[0]
        assertEquals("lesson1", lesson.id)
        assertEquals("Sabah Dersi", lesson.title)
        assertEquals("Ahmet Hoca", lesson.instructorName)
        assertEquals(60, lesson.durationMin)
        assertEquals(3, lesson.spotsAvailable)
        assertEquals(false, lesson.isBookedByMe)
        assertEquals(false, lesson.isFull)
    }

    @Test
    fun `getLessons emits empty list when data source throws`() = runTest {
        whenever(dataSource.getLessons()).thenThrow(RuntimeException("Network error"))

        val result = repository.getLessons().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getLessons marks lesson as full when spotsAvailable is 0`() = runTest {
        val dto = LessonFunctionsDto(
            id = "full1", date = "2026-04-01", title = "Full", instructorName = "Hoca",
            durationMin = 45, level = "advanced", price = 200.0,
            spotsTotal = 3, spotsAvailable = 0, isBookedByMe = false
        )
        whenever(dataSource.getLessons()).thenReturn(listOf(dto))

        val lesson = repository.getLessons().first().first()

        assertTrue(lesson.isFull)
    }

    // ─── bookLesson ──────────────────────────────────────────────────────────

    @Test
    fun `bookLesson returns success with reservation when data source succeeds`() = runTest {
        val dto = ReservationFunctionsDto(
            id = "res1",
            lessonId = "lesson1",
            lessonTitle = "Sabah Dersi",
            lessonDate = "2026-04-01",
            instructorName = "Ahmet Hoca",
            status = "confirmed",
            createdAt = "2026-03-18T10:00:00Z"
        )
        whenever(dataSource.bookLesson("lesson1")).thenReturn(dto)

        val result = repository.bookLesson("lesson1")

        assertTrue(result.isSuccess)
        val reservation = result.getOrThrow()
        assertEquals("res1", reservation.id)
        assertEquals(ReservationStatus.CONFIRMED, reservation.status)
    }

    @Test
    fun `bookLesson returns failure when data source throws`() = runTest {
        whenever(dataSource.bookLesson("lesson1")).thenThrow(RuntimeException("Spots full"))

        val result = repository.bookLesson("lesson1")

        assertTrue(result.isFailure)
        assertEquals("Spots full", result.exceptionOrNull()?.message)
    }

    // ─── cancelReservation ───────────────────────────────────────────────────

    @Test
    fun `cancelReservation returns success when data source succeeds`() = runTest {
        whenever(dataSource.cancelReservation("res1")).thenReturn(Unit)

        val result = repository.cancelReservation("res1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `cancelReservation returns failure when data source throws`() = runTest {
        whenever(dataSource.cancelReservation("res1")).thenThrow(RuntimeException("Not found"))

        val result = repository.cancelReservation("res1")

        assertTrue(result.isFailure)
    }

    // ─── getMyReservations ───────────────────────────────────────────────────

    @Test
    fun `getMyReservations emits mapped reservations`() = runTest {
        val dto = ReservationFunctionsDto(
            id = "res2", lessonId = "l2", lessonTitle = "Akşam Dersi",
            lessonDate = "2026-04-02", instructorName = "Fatma Hoca",
            status = "pending", createdAt = ""
        )
        whenever(dataSource.getMyReservations()).thenReturn(listOf(dto))

        val result = repository.getMyReservations().first()

        assertEquals(1, result.size)
        assertEquals(ReservationStatus.PENDING, result[0].status)
    }

    @Test
    fun `getMyReservations emits empty list on error`() = runTest {
        whenever(dataSource.getMyReservations()).thenThrow(RuntimeException("Auth error"))

        val result = repository.getMyReservations().first()

        assertTrue(result.isEmpty())
    }

    // ─── toDomain status mapping ─────────────────────────────────────────────

    @Test
    fun `toDomain maps confirmed status correctly`() = runTest {
        val dto = reservationDto(status = "confirmed")
        whenever(dataSource.bookLesson("x")).thenReturn(dto)
        val res = repository.bookLesson("x").getOrThrow()
        assertEquals(ReservationStatus.CONFIRMED, res.status)
    }

    @Test
    fun `toDomain maps cancelled status correctly`() = runTest {
        val dto = reservationDto(status = "cancelled")
        whenever(dataSource.bookLesson("x")).thenReturn(dto)
        val res = repository.bookLesson("x").getOrThrow()
        assertEquals(ReservationStatus.CANCELLED, res.status)
    }

    @Test
    fun `toDomain maps completed status correctly`() = runTest {
        val dto = reservationDto(status = "completed")
        whenever(dataSource.bookLesson("x")).thenReturn(dto)
        val res = repository.bookLesson("x").getOrThrow()
        assertEquals(ReservationStatus.COMPLETED, res.status)
    }

    @Test
    fun `toDomain maps unknown status to PENDING`() = runTest {
        val dto = reservationDto(status = "unknown_status")
        whenever(dataSource.bookLesson("x")).thenReturn(dto)
        val res = repository.bookLesson("x").getOrThrow()
        assertEquals(ReservationStatus.PENDING, res.status)
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private fun reservationDto(status: String) = ReservationFunctionsDto(
        id = "r1", lessonId = "l1", lessonTitle = "Test",
        lessonDate = "2026-04-01", instructorName = "Hoca",
        status = status, createdAt = ""
    )
}
