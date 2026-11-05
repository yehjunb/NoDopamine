package com.yehjunb.nodopamine.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yehjunb.nodopamine.data.DetoxState
import com.yehjunb.nodopamine.data.DetoxStore
import com.yehjunb.nodopamine.data.Domains

private val SUGGESTIONS = listOf(
    "youtube.com", "instagram.com", "tiktok.com", "x.com",
    "facebook.com", "reddit.com", "twitch.tv", "namu.wiki",
)

/** 차단할 웹사이트 주소를 직접 입력하는 화면 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteScreen(state: DetoxState, onBack: () -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val locked = state.isBlocking(System.currentTimeMillis())

    fun add(raw: String) {
        error = DetoxStore.addDomain(raw)
        if (error == null && raw == input) input = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("웹사이트 차단") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp),
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it; error = null },
                        placeholder = { Text("예: youtube.com") },
                        singleLine = true,
                        isError = error != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { add(input) }),
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { add(input) }, enabled = input.isNotBlank()) { Text("추가") }
                }
                Text(
                    error ?: "차단이 켜지면 Chrome·삼성 인터넷 등에서 이 사이트와 하위 주소(m.youtube.com 등)가 열리지 않아요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            val remaining = SUGGESTIONS.filter { it !in state.blockedDomains }
            if (remaining.isNotEmpty()) {
                item { SectionTitle("자주 차단하는 사이트") }
                items(remaining, key = { "s-$it" }) { domain ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { add(domain) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Icon(Icons.Filled.AddCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(domain)
                    }
                }
            }

            item {
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                SectionTitle("차단할 사이트 ${state.blockedDomains.size}/${Domains.MAX}")
                if (state.blockedDomains.isEmpty()) {
                    Text(
                        "아직 추가한 사이트가 없어요",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                if (locked && state.blockedDomains.isNotEmpty()) {
                    Text(
                        "차단 중에는 추가만 할 수 있고 지울 수는 없어요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
            items(state.blockedDomains, key = { "d-$it" }) { domain ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp),
                ) {
                    Icon(Icons.Filled.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(domain, modifier = Modifier.weight(1f))
                    IconButton(onClick = { DetoxStore.removeDomain(domain) }, enabled = !locked) {
                        Icon(Icons.Filled.Delete, contentDescription = "$domain 지우기")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
