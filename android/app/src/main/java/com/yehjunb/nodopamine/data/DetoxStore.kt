package com.yehjunb.nodopamine.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

/**
 * 앱의 모든 데이터를 보관하고 폰에 저장하는 창고.
 *
 * 화면(MainActivity)과 차단 서비스(BlockerService)가 같은 창고를 함께 봐요.
 * 값이 바뀌면 state 를 구독하는 화면이 자동으로 다시 그려지고, 폰에도 바로 저장돼요.
 */
object DetoxStore {
    private const val PREFS_NAME = "nodopamine"
    private const val KEY_STATE = "state"
    private const val MAX_RECORDS = 500

    private var prefs: SharedPreferences? = null
    private val _state = MutableStateFlow(DetoxState())
    val state: StateFlow<DetoxState> = _state.asStateFlow()

    /** 앱·서비스·차단 화면이 시작될 때 한 번 불러요. 여러 번 불러도 괜찮아요. */
    @Synchronized
    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = p
        _state.value = p.getString(KEY_STATE, null)
            ?.let { runCatching { fromJson(JSONObject(it)) }.getOrNull() }
            ?: DetoxState()
    }

    @Synchronized
    private fun update(transform: (DetoxState) -> DetoxState) {
        val next = transform(_state.value)
        if (next == _state.value) return
        _state.value = next
        prefs?.edit()?.putString(KEY_STATE, toJson(next).toString())?.apply()
    }

    // ── 차단 목록 ─────────────────────────────

    fun setBlockedPackages(packages: Set<String>) = update { it.copy(blockedPackages = packages) }

    /** 성공하면 null, 실패하면 이유를 돌려줘요. */
    fun addDomain(raw: String): String? {
        val domain = Domains.normalize(raw) ?: return "올바른 주소가 아니에요. 예: youtube.com"
        val current = _state.value.blockedDomains
        if (domain in current) return null
        if (current.size >= Domains.MAX) return "사이트는 최대 ${Domains.MAX}개까지 추가할 수 있어요."
        update { it.copy(blockedDomains = it.blockedDomains + domain) }
        return null
    }

    fun removeDomain(domain: String) = update { it.copy(blockedDomains = it.blockedDomains - domain) }

    // ── 지금 바로 차단 / 예약 차단 ─────────────

    fun setManualBlock(on: Boolean) = update { it.copy(manualBlockOn = on) }

    fun setSchedule(schedule: BlockSchedule) = update { it.copy(schedule = schedule) }

    // ── 집중 세션 ─────────────────────────────

    fun startFocus(minutes: Int) {
        val now = System.currentTimeMillis()
        update {
            if (it.activeFocus != null) it
            else it.copy(activeFocus = ActiveFocus(UUID.randomUUID().toString(), now, now + minutes * 60_000L))
        }
    }

    /** 시간이 다 됐으면 "완료"로 기록해요. */
    fun checkCompletion(nowMillis: Long = System.currentTimeMillis()) {
        val focus = _state.value.activeFocus ?: return
        if (nowMillis >= focus.endMillis) finishFocus(completed = true, nowMillis = nowMillis)
    }

    fun giveUpFocus() = finishFocus(completed = false, nowMillis = System.currentTimeMillis())

    private fun finishFocus(completed: Boolean, nowMillis: Long) = update { s ->
        val focus = s.activeFocus ?: return@update s
        val record = FocusRecord(
            id = focus.id,
            startMillis = focus.startMillis,
            endMillis = if (completed) focus.endMillis else nowMillis,
            plannedMinutes = focus.plannedMinutes,
            completed = completed,
        )
        s.copy(activeFocus = null, records = (listOf(record) + s.records).take(MAX_RECORDS))
    }

    fun setDailyGoal(minutes: Int) = update { it.copy(dailyGoalMinutes = minutes.coerceIn(15, 480)) }

    // ── 기타 ─────────────────────────────────

    fun recordResist(day: LocalDate = LocalDate.now()) = update {
        val key = day.toString()
        it.copy(resistCounts = it.resistCounts + (key to (it.resistCounts[key] ?: 0) + 1))
    }

    fun toggleSimplifyDone(id: String) = update {
        it.copy(simplifyDone = if (id in it.simplifyDone) it.simplifyDone - id else it.simplifyDone + id)
    }

    // ── 저장 형식 (JSON) ──────────────────────

    private fun toJson(s: DetoxState): JSONObject = JSONObject().apply {
        put("blockedPackages", JSONArray(s.blockedPackages.toList()))
        put("blockedDomains", JSONArray(s.blockedDomains))
        put("manualBlockOn", s.manualBlockOn)
        put("schedule", JSONObject().apply {
            put("enabled", s.schedule.enabled)
            put("startHour", s.schedule.startHour)
            put("startMinute", s.schedule.startMinute)
            put("endHour", s.schedule.endHour)
            put("endMinute", s.schedule.endMinute)
        })
        s.activeFocus?.let { f ->
            put("activeFocus", JSONObject().apply {
                put("id", f.id)
                put("startMillis", f.startMillis)
                put("endMillis", f.endMillis)
            })
        }
        put("records", JSONArray().apply {
            s.records.forEach { r ->
                put(JSONObject().apply {
                    put("id", r.id)
                    put("startMillis", r.startMillis)
                    put("endMillis", r.endMillis)
                    put("plannedMinutes", r.plannedMinutes)
                    put("completed", r.completed)
                })
            }
        })
        put("dailyGoalMinutes", s.dailyGoalMinutes)
        put("resistCounts", JSONObject().apply { s.resistCounts.forEach { (k, v) -> put(k, v) } })
        put("simplifyDone", JSONArray(s.simplifyDone.toList()))
    }

    private fun fromJson(o: JSONObject): DetoxState {
        fun JSONArray?.strings(): List<String> =
            if (this == null) emptyList() else (0 until length()).map { getString(it) }

        val schedule = o.optJSONObject("schedule")?.let {
            BlockSchedule(
                enabled = it.optBoolean("enabled"),
                startHour = it.optInt("startHour", 22),
                startMinute = it.optInt("startMinute", 0),
                endHour = it.optInt("endHour", 7),
                endMinute = it.optInt("endMinute", 0),
            )
        } ?: BlockSchedule()

        val focus = o.optJSONObject("activeFocus")?.let {
            ActiveFocus(it.getString("id"), it.getLong("startMillis"), it.getLong("endMillis"))
        }

        val recordsJson = o.optJSONArray("records")
        val records = if (recordsJson == null) emptyList() else (0 until recordsJson.length()).map { i ->
            val r = recordsJson.getJSONObject(i)
            FocusRecord(
                id = r.getString("id"),
                startMillis = r.getLong("startMillis"),
                endMillis = r.getLong("endMillis"),
                plannedMinutes = r.getInt("plannedMinutes"),
                completed = r.getBoolean("completed"),
            )
        }

        val resistJson = o.optJSONObject("resistCounts")
        val resists = buildMap<String, Int> {
            if (resistJson != null) {
                for (key in resistJson.keys()) put(key, resistJson.getInt(key))
            }
        }

        return DetoxState(
            blockedPackages = o.optJSONArray("blockedPackages").strings().toSet(),
            blockedDomains = o.optJSONArray("blockedDomains").strings(),
            manualBlockOn = o.optBoolean("manualBlockOn"),
            schedule = schedule,
            activeFocus = focus,
            records = records,
            dailyGoalMinutes = o.optInt("dailyGoalMinutes", 60),
            resistCounts = resists,
            simplifyDone = o.optJSONArray("simplifyDone").strings().toSet(),
        )
    }
}
