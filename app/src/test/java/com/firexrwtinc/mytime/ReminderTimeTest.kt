package com.firexrwtinc.mytime

import com.firexrwtinc.mytime.data.model.Task
import com.firexrwtinc.mytime.notifications.NotificationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Unit tests for [NotificationHelper.reminderTimeFor].
 * Plain JUnit4 - no Robolectric/Mockito, no extra test dependencies.
 */
class ReminderTimeTest {

    private fun taskAt(date: LocalDate, startTime: LocalTime, reminderHoursBefore: Int?): Task {
        return Task(
            title = "Test Task",
            date = date,
            startTime = startTime,
            endTime = startTime.plusHours(1),
            reminderHoursBefore = reminderHoursBefore,
            colorHex = "#82B1FF"
        )
    }

    @Test
    fun `reminder two hours before 14 00 fires at 12 00 same day`() {
        val task = taskAt(LocalDate.of(2026, 7, 25), LocalTime.of(14, 0), 2)

        val result = NotificationHelper.reminderTimeFor(task)

        assertEquals(LocalDateTime.of(2026, 7, 25, 12, 0), result)
    }

    @Test
    fun `null reminderHoursBefore returns null`() {
        val task = taskAt(LocalDate.of(2026, 7, 25), LocalTime.of(14, 0), null)

        assertNull(NotificationHelper.reminderTimeFor(task))
    }

    @Test
    fun `zero reminderHoursBefore returns null`() {
        val task = taskAt(LocalDate.of(2026, 7, 25), LocalTime.of(14, 0), 0)

        assertNull(NotificationHelper.reminderTimeFor(task))
    }

    @Test
    fun `negative reminderHoursBefore returns null`() {
        val task = taskAt(LocalDate.of(2026, 7, 25), LocalTime.of(14, 0), -1)

        assertNull(NotificationHelper.reminderTimeFor(task))
    }

    @Test
    fun `reminder crossing midnight lands on previous day`() {
        val task = taskAt(LocalDate.of(2026, 7, 25), LocalTime.of(1, 0), 3)

        val result = NotificationHelper.reminderTimeFor(task)

        assertEquals(LocalDateTime.of(2026, 7, 24, 22, 0), result)
    }
}
