package com.yehjunb.nodopamine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yehjunb.nodopamine.data.ActiveFocus
import com.yehjunb.nodopamine.data.DetoxState
import com.yehjunb.nodopamine.data.DetoxStore
import com.yehjunb.nodopamine.data.clockLabel
import com.yehjunb.nodopamine.data.durationLabel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val DURATIONS = listOf(15, 30, 45, 60, 90, 120)

/** "디톡스" 탭: 집중 세션 시작, 남은 시간, 연속 기록과 통계 */
@Composable
fun ChallengeScreen(state: DetoxState, contentPadding: PaddingValues) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedMinutes by rememberSaveable { mutableIntStateOf(30) }
    var giveUp by remember { mutableStateOf<UnlockRequest?>(null) }

    // 1초마다 시계를 갱신하고, 끝난 세션이 있으면 "완료"로 기록해요.
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            DetoxStore.checkCompletion(now)
            delay(1000)
        }
    }

    val today = LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val focus = state.activeFocus
        if (focus != null && state.isFocusRunning(now)) {
            RunningCard(focus, now) {
                giveUp = UnlockRequest(
                    title = "정말 그만둘까요?",
                    message = "포기하면 이 세션은 '실패'로 기록되고 집중 시간에 포함되지 않아요.",
                    confirmText = "포기하기",
                    action = { DetoxStore.giveUpFocus() },
                )
            }
        } else {
            StartCard(
                selectedMinutes = selectedMinutes,
                onSelect = { selectedMinutes = it },
                nothingSelected = state.blockedPackages.isEmpty() && state.blockedDomains.isEmpty(),
                onStart = { DetoxStore.startFocus(selectedMinutes) },
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(Icons.Filled.LocalFireDepartment, "${state.streak(today)}일", "연속 달성", Modifier.weight(1f))
            StatTile(Icons.Filled.Timer, "${state.focusMinutesOn(today)}분", "오늘 집중", Modifier.weight(1f))
            StatTile(Icons.Filled.ThumbUp, "${state.resistCount(today)}번", "오늘 참은 횟수", Modifier.weight(1f))
        }

        GoalCard(state, today)
        WeekCard(state, today)
        HistoryCard(state)
    }

    giveUp?.let { CooldownDialog(it) { giveUp = null } }
}

@Composable
private fun StartCard(
    selectedMinutes: Int,
    onSelect: (Int) -> Unit,
    nothingSelected: Boolean,
    onStart: () -> Unit,
) {
    SectionCard {
        Text("집중 세션", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "시간을 정하면 그동안 선택한 앱과 사이트가 잠겨요.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DURATIONS.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { minutes ->
                    val selected = minutes == selectedMinutes
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp),
                            )
                            .clickable { onSelect(minutes) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            durationLabel(minutes),
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
        if (nothingSelected) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFE69500), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "아직 차단할 앱을 고르지 않았어요. '차단' 탭에서 먼저 골라주세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE69500),
                )
            }
        }
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("${durationLabel(selectedMinutes)} 집중 시작", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RunningCard(focus: ActiveFocus, now: Long, onGiveUp: () -> Unit) {
    val total = (focus.endMillis - focus.startMillis).coerceAtLeast(1)
    val remaining = (focus.endMillis - now).coerceAtLeast(0)
    val progress = 1f - remaining.toFloat() / total

    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("집중하는 중", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(220.dp),
                    strokeWidth = 14.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(clockLabel(remaining), fontSize = 44.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${durationLabel(focus.plannedMinutes)} 세션",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                "선택한 앱과 사이트가 잠겨 있어요.\n폰을 내려놓고 하고 싶었던 일을 해봐요.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = onGiveUp) {
                Text("포기하기", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun GoalCard(state: DetoxState, today: LocalDate) {
    val goal = state.dailyGoalMinutes
    val done = state.focusMinutesOn(today)
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("하루 목표", modifier = Modifier.weight(1f))
            FilledTonalIconButton(onClick = { DetoxStore.setDailyGoal(goal - 15) }, enabled = goal > 15) {
                Icon(Icons.Filled.Remove, contentDescription = "목표 줄이기")
            }
            Text(
                durationLabel(goal),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            FilledTonalIconButton(onClick = { DetoxStore.setDailyGoal(goal + 15) }, enabled = goal < 480) {
                Icon(Icons.Filled.Add, contentDescription = "목표 늘리기")
            }
        }
        LinearProgressIndicator(
            progress = { (done.toFloat() / goal).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            if (done >= goal) "오늘 목표 달성! 🎉"
            else "목표까지 ${goal - done}분 남았어요. 목표를 채운 날이 이어지면 연속 달성일이 늘어요.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WeekCard(state: DetoxState, today: LocalDate) {
    val days = state.lastSevenDays(today)
    val goal = state.dailyGoalMinutes
    val maxMinutes = maxOf(goal, days.maxOf { it.minutes }, 1)

    SectionCard {
        Text("최근 7일", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            days.forEach { stat ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("${stat.minutes}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((110f * stat.minutes / maxMinutes).coerceAtLeast(4f).dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = if (stat.minutes >= goal) 1f else 0.35f),
                                RoundedCornerShape(6.dp),
                            ),
                    )
                    Text(
                        stat.day.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(state: DetoxState) {
    val formatter = remember { DateTimeFormatter.ofPattern("M/d HH:mm") }
    SectionCard {
        Row {
            Text("기록", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("완료 ${state.completedCount}회", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (state.records.isEmpty()) {
            Text("아직 기록이 없어요. 첫 집중 세션을 시작해 보세요!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        state.records.take(10).forEach { record ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (record.completed) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = null,
                    tint = if (record.completed) Color(0xFF34A853) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                val lasted = ((record.endMillis - record.startMillis) / 60_000).toInt()
                Text(
                    if (record.completed) "${durationLabel(record.plannedMinutes)} 집중 완료" else "${lasted}분 만에 포기",
                    modifier = Modifier.weight(1f),
                )
                val start = java.time.Instant.ofEpochMilli(record.startMillis)
                    .atZone(java.time.ZoneId.systemDefault())
                Text(
                    formatter.format(start),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
