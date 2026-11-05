package com.yehjunb.nodopamine.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yehjunb.nodopamine.data.AppInfo
import com.yehjunb.nodopamine.data.DetoxState
import com.yehjunb.nodopamine.data.DetoxStore
import com.yehjunb.nodopamine.data.InstalledApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** "차단" 탭: 무엇을, 언제 막을지 정해요. */
@Composable
fun BlockScreen(
    state: DetoxState,
    serviceEnabled: Boolean,
    contentPadding: PaddingValues,
    onPickApps: () -> Unit,
    onEditWebsites: () -> Unit,
) {
    val context = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var unlock by remember { mutableStateOf<UnlockRequest?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(15_000)
        }
    }

    val apps by produceState<List<AppInfo>>(emptyList()) {
        value = withContext(Dispatchers.IO) { InstalledApps.load(context) }
    }
    val blockedApps = apps.filter { it.packageName in state.blockedPackages }

    val reasons = state.activeReasons(now)
    val scheduleActive = state.isScheduleActive(now)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!serviceEnabled) {
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("접근성 권한이 꺼져 있어서 차단이 동작하지 않아요", fontWeight = FontWeight.Bold)
                }
                Text(
                    "접근성 설정 → 설치된 앱 → NoDopamine 을 켜주세요. 막혀 있으면 앱 정보 → ⋮ → '제한된 설정 허용'을 먼저 눌러요.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { openAccessibilitySettings(context) }) { Text("접근성 설정") }
                    FilledTonalButton(onClick = { openAppInfo(context) }) { Text("앱 정보") }
                }
            }
        }

        // 현재 상태
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (reasons.isEmpty()) Icons.Filled.LockOpen else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = if (reasons.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    if (reasons.isEmpty()) "지금은 아무것도 차단하지 않고 있어요"
                    else "차단 중 · ${reasons.joinToString(", ")}",
                    fontWeight = if (reasons.isEmpty()) FontWeight.Normal else FontWeight.Bold,
                    color = if (reasons.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                )
            }
        }

        // 차단 목록
        SectionCard {
            Text("차단 목록", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            NavRow(Icons.Filled.Block, "차단할 앱 고르기", "${state.blockedPackages.size}개", onPickApps)
            blockedApps.forEach { app ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
                    Image(app.icon, contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(app.label)
                }
            }
            NavRow(Icons.Filled.Language, "차단할 웹사이트", "${state.blockedDomains.size}개", onEditWebsites)
            Text(
                if (reasons.isNotEmpty()) "차단이 켜져 있는 동안에는 목록에 추가만 할 수 있고 뺄 수는 없어요."
                else "인스타그램, 유튜브, 틱톡처럼 무심코 열게 되는 앱을 골라보세요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // 지금 바로 차단
        SectionCard {
            SwitchRow(
                icon = Icons.Filled.Bolt,
                title = "지금 바로 차단",
                checked = state.manualBlockOn,
                onCheckedChange = { on ->
                    if (on) {
                        DetoxStore.setManualBlock(true)
                    } else {
                        unlock = UnlockRequest(
                            title = "차단을 풀까요?",
                            message = "지금 열고 싶은 그 앱, 10초 뒤에도 정말 필요할까요?",
                            confirmText = "차단 풀기",
                            action = { DetoxStore.setManualBlock(false) },
                        )
                    }
                },
            )
            Text(
                "켜면 직접 끌 때까지 계속 차단돼요. 끌 때는 10초를 기다려야 해요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // 예약 차단
        SectionCard {
            val schedule = state.schedule
            SwitchRow(
                icon = Icons.Filled.Bedtime,
                title = "매일 예약 차단",
                checked = schedule.enabled,
                onCheckedChange = { on ->
                    if (!on && scheduleActive) {
                        unlock = UnlockRequest(
                            title = "예약 차단을 끌까요?",
                            message = "지금은 스스로 정한 차단 시간이에요.",
                            confirmText = "예약 끄기",
                            action = { DetoxStore.setSchedule(DetoxStore.state.value.schedule.copy(enabled = false)) },
                        )
                    } else {
                        DetoxStore.setSchedule(schedule.copy(enabled = on))
                    }
                },
            )
            if (schedule.enabled) {
                TimeRow("시작", schedule.startHour, schedule.startMinute, enabled = !scheduleActive) { h, m ->
                    DetoxStore.setSchedule(schedule.copy(startHour = h, startMinute = m))
                }
                TimeRow("끝", schedule.endHour, schedule.endMinute, enabled = !scheduleActive) { h, m ->
                    DetoxStore.setSchedule(schedule.copy(endHour = h, endMinute = m))
                }
            }
            Text(
                "예: 밤 10시 ~ 아침 7시. 끝 시간이 시작보다 빠르면 다음 날까지 이어져요." +
                    if (scheduleActive) "\n지금은 차단 시간이라 시간을 바꿀 수 없어요." else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    unlock?.let { CooldownDialog(it) { unlock = null } }
}

@Composable
private fun NavRow(icon: ImageVector, title: String, trailing: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(title, modifier = Modifier.weight(1f))
        Text(trailing, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SwitchRow(icon: ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun TimeRow(label: String, hour: Int, minute: Int, enabled: Boolean, onPicked: (Int, Int) -> Unit) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                TimePickerDialog(context, { _, h, m -> onPicked(h, m) }, hour, minute, false).show()
            }
            .padding(vertical = 8.dp),
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Text(
            formatTime(hour, minute),
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 22, 0 → "오후 10:00" */
private fun formatTime(hour: Int, minute: Int): String {
    val ampm = if (hour < 12) "오전" else "오후"
    val h12 = if (hour % 12 == 0) 12 else hour % 12
    return "%s %d:%02d".format(ampm, h12, minute)
}
