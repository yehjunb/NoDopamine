package com.yehjunb.nodopamine.data

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.roundToInt

// 이 파일은 안드로이드 기능을 쓰지 않는 "순수 계산"만 모아둔 곳이에요.
// 그래서 폰 없이도 단위 테스트(app/src/test)로 검사할 수 있어요.

/** 매일 반복되는 예약 차단 시간 (예: 밤 10시 ~ 아침 7시) */
data class BlockSchedule(
    val enabled: Boolean = false,
    val startHour: Int = 22,
    val startMinute: Int = 0,
    val endHour: Int = 7,
    val endMinute: Int = 0,
) {
    /** 주어진 시각이 차단 시간 안인지. 시작이 끝보다 늦으면 자정을 넘어가는 것으로 봐요. */
    fun contains(time: LocalTime): Boolean {
        if (!enabled) return false
        val now = time.hour * 60 + time.minute
        val start = startHour * 60 + startMinute
        val end = endHour * 60 + endMinute
        if (start == end) return false
        return if (start < end) now in start until end else (now >= start || now < end)
    }
}

/** 진행 중인 집중 세션 */
data class ActiveFocus(
    val id: String,
    val startMillis: Long,
    val endMillis: Long,
) {
    val plannedMinutes: Int get() = ((endMillis - startMillis) / 60_000.0).roundToInt()
}

/** 끝난 집중 세션 기록 */
data class FocusRecord(
    val id: String,
    val startMillis: Long,
    val endMillis: Long,
    val plannedMinutes: Int,
    val completed: Boolean,
)

data class DayStat(val day: LocalDate, val minutes: Int)

/** 앱 전체 상태. 화면과 차단 서비스가 모두 이 값을 봐요. */
data class DetoxState(
    val blockedPackages: Set<String> = emptySet(),
    val blockedDomains: List<String> = emptyList(),
    val manualBlockOn: Boolean = false,
    val schedule: BlockSchedule = BlockSchedule(),
    val activeFocus: ActiveFocus? = null,
    val records: List<FocusRecord> = emptyList(),
    val dailyGoalMinutes: Int = 60,
    /** "2026-11-05" → 그날 차단 화면에서 "참았다!"를 누른 횟수 */
    val resistCounts: Map<String, Int> = emptyMap(),
    /** 화면 단순화 체크리스트에서 완료한 항목 */
    val simplifyDone: Set<String> = emptySet(),
) {
    fun isFocusRunning(nowMillis: Long): Boolean =
        activeFocus != null && nowMillis < activeFocus.endMillis

    fun isScheduleActive(nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Boolean =
        schedule.contains(Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalTime())

    /** 지금 차단이 걸려 있는 이유들 (비어 있으면 차단 안 함) */
    fun activeReasons(nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): List<String> = buildList {
        if (isFocusRunning(nowMillis)) add("집중 세션")
        if (manualBlockOn) add("지금 바로 차단")
        if (isScheduleActive(nowMillis, zone)) add("예약 차단")
    }

    fun isBlocking(nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Boolean =
        activeReasons(nowMillis, zone).isNotEmpty()

    /** 그날 완료한 집중 시간(분). 포기한 세션은 넣지 않아요. */
    fun focusMinutesOn(day: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Int =
        records.filter { it.completed && it.startMillis.toLocalDate(zone) == day }
            .sumOf { it.plannedMinutes }

    /** 연속 달성일: 하루 목표를 채운 날이 며칠 연속인지. 오늘 아직 못 채웠어도 어제까지 이어졌으면 유지. */
    fun streak(today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Int {
        if (dailyGoalMinutes <= 0) return 0
        var day = today
        if (focusMinutesOn(day, zone) < dailyGoalMinutes) day = day.minusDays(1)
        var count = 0
        while (focusMinutesOn(day, zone) >= dailyGoalMinutes) {
            count++
            day = day.minusDays(1)
        }
        return count
    }

    /** 최근 7일 (오래된 날 → 오늘) */
    fun lastSevenDays(today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): List<DayStat> =
        (6 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong())
            DayStat(day, focusMinutesOn(day, zone))
        }

    fun resistCount(day: LocalDate): Int = resistCounts[day.toString()] ?: 0

    val completedCount: Int get() = records.count { it.completed }
}

fun Long.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

/** 30 → "30분", 60 → "1시간", 90 → "1시간 30분" */
fun durationLabel(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "${m}분"
        m == 0 -> "${h}시간"
        else -> "${h}시간 ${m}분"
    }
}

/** 남은 밀리초 → "25:00" 또는 "1:05:00" */
fun clockLabel(millis: Long): String {
    val total = ((millis + 999) / 1000).coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
