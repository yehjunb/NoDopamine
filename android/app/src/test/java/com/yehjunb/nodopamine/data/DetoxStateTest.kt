package com.yehjunb.nodopamine.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class DetoxStateTest {

    private val zone = ZoneId.of("Asia/Seoul")

    private fun millis(date: LocalDate, hour: Int, minute: Int = 0): Long =
        date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

    @Test
    fun `자정을 넘는 예약 차단`() {
        val night = BlockSchedule(enabled = true, startHour = 22, endHour = 7)
        assertTrue(night.contains(LocalTime.of(23, 30)))
        assertTrue(night.contains(LocalTime.of(2, 0)))
        assertTrue(night.contains(LocalTime.of(22, 0)))
        assertFalse(night.contains(LocalTime.of(7, 0)))
        assertFalse(night.contains(LocalTime.of(12, 0)))
    }

    @Test
    fun `낮 시간 예약 차단과 꺼진 예약`() {
        val work = BlockSchedule(enabled = true, startHour = 9, endHour = 18)
        assertTrue(work.contains(LocalTime.of(9, 0)))
        assertFalse(work.contains(LocalTime.of(18, 0)))
        assertFalse(work.copy(enabled = false).contains(LocalTime.of(10, 0)))
    }

    @Test
    fun `집중 세션 중에만 차단된다`() {
        val today = LocalDate.of(2026, 11, 5)
        val state = DetoxState(activeFocus = ActiveFocus("a", millis(today, 10), millis(today, 11)))
        assertTrue(state.isBlocking(millis(today, 10, 30), zone))
        assertFalse(state.isBlocking(millis(today, 11, 1), zone))
        assertEquals(listOf("집중 세션"), state.activeReasons(millis(today, 10, 30), zone))
    }

    @Test
    fun `연속 달성일 계산`() {
        val today = LocalDate.of(2026, 11, 5)
        fun done(day: LocalDate, minutes: Int, completed: Boolean = true) =
            FocusRecord("$day-$minutes", millis(day, 9), millis(day, 9) + minutes * 60_000L, minutes, completed)

        val state = DetoxState(
            dailyGoalMinutes = 60,
            records = listOf(
                done(today.minusDays(1), 60),
                done(today.minusDays(2), 30),
                done(today.minusDays(2), 30),
                done(today.minusDays(3), 90, completed = false), // 포기한 날 → 끊김
                done(today.minusDays(4), 120),
            ),
        )
        // 오늘은 아직이지만 어제·그제 연속 달성 → 2일
        assertEquals(2, state.streak(today, zone))
        assertEquals(60, state.focusMinutesOn(today.minusDays(2), zone))
        assertEquals(0, state.focusMinutesOn(today.minusDays(3), zone))

        val week = state.lastSevenDays(today, zone)
        assertEquals(7, week.size)
        assertEquals(today, week.last().day)
    }

    @Test
    fun `시간 표시`() {
        assertEquals("30분", durationLabel(30))
        assertEquals("1시간", durationLabel(60))
        assertEquals("1시간 30분", durationLabel(90))
        assertEquals("25:00", clockLabel(25 * 60_000L))
        assertEquals("1:05:00", clockLabel(65 * 60_000L))
        assertEquals("00:01", clockLabel(1))
    }
}
