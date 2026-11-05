package com.yehjunb.nodopamine.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

fun openAccessibilitySettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

fun openAppInfo(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}

/** 처음 실행할 때: 차단에 필요한 "접근성" 권한을 켜도록 안내하는 화면 */
@Composable
fun OnboardingScreen(onSkip: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Icon(Icons.Filled.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(84.dp))
        Text("NoDopamine", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(
            "자극적인 앱과 사이트를 잠그고\n폰 대신 내 시간을 되찾아요.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Feature(Icons.Filled.Block, "앱·웹사이트 차단 (지금 바로 / 매일 예약)")
            Feature(Icons.Filled.Timer, "집중 세션과 연속 달성 기록")
            Feature(Icons.Filled.Contrast, "흑백 화면 등 폰을 덜 자극적으로 바꾸기")
        }

        SectionCard {
            Text("차단하려면 권한 하나가 필요해요", fontWeight = FontWeight.Bold)
            Text(
                "다른 앱이 열린 걸 알아채려면 안드로이드의 '접근성' 권한이 필요해요. " +
                    "앱 이름과 브라우저 주소창만 확인하고, 화면 내용을 저장하거나 밖으로 보내지 않아요.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "1. 아래 버튼으로 접근성 설정을 열어요.\n" +
                    "2. '설치된 앱'(또는 '설치된 서비스')에서 NoDopamine 을 눌러요.\n" +
                    "3. 사용을 켜고 '허용'을 눌러요.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = { openAccessibilitySettings(context) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("접근성 설정 열기", fontWeight = FontWeight.SemiBold)
            }
        }

        SectionCard {
            Text("'제한된 설정'이라고 막혀 있다면", fontWeight = FontWeight.Bold)
            Text(
                "Play 스토어가 아닌 곳에서 설치한 앱은 안드로이드가 접근성 권한을 막아둬요.\n" +
                    "1. 아래 버튼으로 앱 정보를 열어요.\n" +
                    "2. 오른쪽 위 ⋮ 메뉴 → '제한된 설정 허용'을 눌러요.\n" +
                    "3. 다시 접근성 설정에서 켜면 돼요.",
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(onClick = { openAppInfo(context) }, modifier = Modifier.fillMaxWidth()) {
                Text("앱 정보 열기")
            }
        }

        TextButton(onClick = onSkip) { Text("나중에 할게요") }
    }
}

@Composable
private fun Feature(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(14.dp))
        Text(text)
    }
}
