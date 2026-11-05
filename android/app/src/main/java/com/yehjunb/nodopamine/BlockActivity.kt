package com.yehjunb.nodopamine

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yehjunb.nodopamine.data.DetoxStore
import com.yehjunb.nodopamine.ui.theme.LeafGreen
import kotlin.math.ceil

/** 차단된 앱이나 사이트를 열면 그 위에 덮이는 "잠깐, 쉬어가요" 화면 */
class BlockActivity : ComponentActivity() {

    private var target by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DetoxStore.init(this)
        target = intent.getStringExtra(EXTRA_TARGET).orEmpty()

        // 뒤로 가기를 눌러도 차단된 앱으로 돌아가지 않고 홈 화면으로 가요.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goHome()
        })

        setContent {
            BlockContent(
                target = target,
                onClose = {
                    DetoxStore.recordResist()
                    goHome()
                },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        target = intent.getStringExtra(EXTRA_TARGET) ?: target
    }

    private fun goHome() {
        startActivity(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        finish()
    }

    companion object {
        const val EXTRA_TARGET = "target"
    }
}

@Composable
private fun BlockContent(target: String, onClose: () -> Unit) {
    val focus = DetoxStore.state.value.activeFocus
    val now = System.currentTimeMillis()
    val message = if (focus != null && focus.endMillis > now) {
        val minutesLeft = ceil((focus.endMillis - now) / 60_000.0).toInt()
        "집중 세션이 ${minutesLeft}분 남았어요.\n조금만 더 버텨봐요."
    } else {
        "지금 꼭 필요한 일인가요?\n숨을 한 번 깊게 쉬고, 하던 일로 돌아가요."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Filled.Spa, contentDescription = null, tint = LeafGreen, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(24.dp))
        Text("잠깐, 쉬어가요", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (target.isNotEmpty()) {
            Text("$target 차단 중", color = Color(0xFFBBBBBB), fontSize = 16.sp)
            Spacer(Modifier.height(20.dp))
        }
        Text(message, color = Color(0xFFBBBBBB), fontSize = 16.sp, textAlign = TextAlign.Center, lineHeight = 24.sp)
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
        ) {
            Text("참았다! 닫기", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
