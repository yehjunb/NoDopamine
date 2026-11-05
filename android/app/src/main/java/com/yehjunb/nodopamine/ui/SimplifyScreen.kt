package com.yehjunb.nodopamine.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.yehjunb.nodopamine.data.DetoxState
import com.yehjunb.nodopamine.data.DetoxStore

/**
 * "화면 단순화" 탭.
 * 안드로이드도 보안상 다른 앱이 화면 색이나 홈 화면을 마음대로 바꾸지 못해요.
 * 그래서 설정 방법을 단계별로 알려주고, 해둔 것을 체크해 두는 체크리스트예요.
 */
@Composable
fun SimplifyScreen(state: DetoxState, contentPadding: PaddingValues) {
    val doneCount = TIPS.count { it.id in state.simplifyDone }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionCard {
            Text("$doneCount / ${TIPS.size} 완료", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(progress = { doneCount.toFloat() / TIPS.size }, modifier = Modifier.fillMaxWidth())
            Text(
                "색이 사라지고 화면이 단순해지면, 앱이 주는 자극이 확 줄어들어요. 하나씩 따라 해보세요.\n" +
                    "메뉴 이름은 기종마다 조금 달라요. 설정 맨 위 검색창에 굵게 표시된 단어를 검색하면 빨라요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TIPS.forEach { tip ->
            TipCard(tip, done = tip.id in state.simplifyDone)
        }
    }
}

@Composable
private fun TipCard(tip: Tip, done: Boolean) {
    var expanded by rememberSaveable(tip.id) { mutableStateOf(false) }
    SectionCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        ) {
            Icon(
                if (done) Icons.Filled.CheckCircle else tip.icon,
                contentDescription = null,
                tint = if (done) Color(0xFF34A853) else MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                tip.title,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold,
                textDecoration = if (done) TextDecoration.LineThrough else null,
                color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
            Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
        }
        AnimatedVisibility(expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(tip.why, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                tip.steps.forEachIndexed { index, step ->
                    Row {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("${index + 1}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(step, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                OutlinedButton(onClick = { DetoxStore.toggleSimplifyDone(tip.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (done) "완료 취소" else "다 했어요")
                }
            }
        }
    }
}

private data class Tip(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val why: String,
    val steps: List<String>,
)

private val TIPS = listOf(
    Tip(
        id = "grayscale",
        icon = Icons.Filled.Contrast,
        title = "화면을 흑백으로 바꾸기",
        why = "빨간 알림 배지, 화려한 썸네일은 뇌를 자극하도록 만들어졌어요. 흑백으로 바꾸면 폰이 금방 지루해져요.",
        steps = listOf(
            "설정 검색창에 '회색조' 또는 '그레이스케일'을 검색해요.",
            "삼성: 설정 > 접근성 > 시인성 향상 > 색상 조정 > 회색조",
            "픽셀: 설정 > 접근성 > 색상 및 움직임 > 색상 보정 > 그레이스케일",
        ),
    ),
    Tip(
        id = "grayscale-shortcut",
        icon = Icons.Filled.TouchApp,
        title = "버튼 하나로 흑백 켜고 끄기",
        why = "사진을 볼 때처럼 색이 꼭 필요할 때만 잠깐 켤 수 있어요.",
        steps = listOf(
            "흑백 설정 화면(위 항목)에서 '바로가기'를 켜요.",
            "버튼 방식(예: 음량 버튼 두 개 길게 누르기, 측면 버튼)을 골라요.",
            "이제 그 버튼으로 흑백을 켜고 끌 수 있어요.",
        ),
    ),
    Tip(
        id = "bedtime",
        icon = Icons.Filled.Bedtime,
        title = "잘 시간엔 자동으로 흑백",
        why = "자기 전 폰을 붙잡는 습관을 줄여줘요. NoDopamine 예약 차단과 같이 쓰면 좋아요.",
        steps = listOf(
            "설정 검색창에 '취침 모드' 또는 '수면 모드'를 검색해요.",
            "픽셀: 디지털 웰빙 > 취침 모드 > 화면 옵션에서 '그레이스케일'을 켜요.",
            "삼성: 모드 및 루틴 > 수면 모드에서 '회색조'를 켜요.",
            "시간을 NoDopamine 예약 차단과 맞춰 두세요.",
        ),
    ),
    Tip(
        id = "home-screen",
        icon = Icons.Filled.Apps,
        title = "홈 화면 첫 페이지 비우기",
        why = "폰을 켜자마자 보이는 앱이 적을수록 무심코 누르는 일이 줄어요.",
        steps = listOf(
            "자주 '무심코' 여는 앱 아이콘을 길게 눌러 '홈 화면에서 삭제'(앱 자체는 남아요)를 골라요.",
            "전화·메시지·지도·캘린더처럼 꼭 필요한 앱만 첫 페이지에 남겨요.",
            "홈 화면 빈 곳을 길게 눌러 '설정' > '새 앱을 홈 화면에 추가'를 꺼요.",
        ),
    ),
    Tip(
        id = "badges",
        icon = Icons.Filled.NotificationsOff,
        title = "알림 배지·알림 줄이기",
        why = "숫자 배지와 알림은 '확인해야 할 것 같은' 불안감을 만들어요.",
        steps = listOf(
            "설정 검색창에 '앱 아이콘 배지' 또는 '알림 표시 점'을 검색해 꺼요.",
            "설정 > 알림 > 앱 알림에서 SNS·쇼핑·게임 앱의 알림을 꺼요.",
            "메신저처럼 꼭 필요한 앱만 남겨요.",
        ),
    ),
    Tip(
        id = "dnd",
        icon = Icons.Filled.DoNotDisturbOn,
        title = "방해 금지 모드 예외 정리",
        why = "꼭 필요한 연락만 받고 나머지 알림은 조용히 하게 해요.",
        steps = listOf(
            "설정 검색창에 '방해 금지'를 검색해요.",
            "예외로 허용할 사람·앱을 꼭 필요한 것만 남겨요.",
            "일정(예: 평일 밤)을 정해 자동으로 켜지게 해요.",
        ),
    ),
    Tip(
        id = "wallpaper",
        icon = Icons.Filled.Wallpaper,
        title = "배경화면을 단색으로",
        why = "화려한 배경 대신 단색 배경은 눈과 뇌를 덜 피곤하게 해요.",
        steps = listOf(
            "홈 화면 빈 곳을 길게 눌러 '배경화면'을 골라요.",
            "'색상' 또는 단색 배경을 골라 검정이나 회색 같은 차분한 색을 선택해요.",
            "다크 모드도 함께 켜면 좋아요. (설정 > 디스플레이 > 다크 모드)",
        ),
    ),
)
