package com.yehjunb.nodopamine.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** 둥근 카드 모양 상자 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

/** 숫자 하나를 크게 보여주는 작은 타일 */
@Composable
fun StatTile(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** "차단 풀기" 같은 행동을 10초 기다림 뒤로 미루기 위한 요청서 */
data class UnlockRequest(
    val title: String,
    val message: String,
    val confirmText: String,
    val action: () -> Unit,
)

/**
 * 차단을 풀거나 세션을 포기하기 전에 10초 동안 기다리게 하는 창.
 * "충동"은 보통 몇 초면 지나가요. 그 몇 초를 벌어주는 장치예요.
 */
@Composable
fun CooldownDialog(request: UnlockRequest, onDismiss: () -> Unit) {
    var secondsLeft by remember(request) { mutableIntStateOf(10) }
    LaunchedEffect(request) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Air, contentDescription = null) },
        title = { Text(request.title) },
        text = {
            Column {
                Text(request.message)
                Spacer(Modifier.height(16.dp))
                Text(
                    if (secondsLeft > 0) "${secondsLeft}초만 더 생각해 봐요" else "그래도 원한다면 괜찮아요",
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("계속 지키기") }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    request.action()
                    onDismiss()
                },
                enabled = secondsLeft == 0,
            ) {
                Text(
                    if (secondsLeft > 0) "${request.confirmText} (${secondsLeft})" else request.confirmText,
                    color = if (secondsLeft == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
