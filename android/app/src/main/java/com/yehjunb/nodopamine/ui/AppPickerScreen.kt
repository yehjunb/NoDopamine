package com.yehjunb.nodopamine.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yehjunb.nodopamine.data.AppInfo
import com.yehjunb.nodopamine.data.DetoxState
import com.yehjunb.nodopamine.data.DetoxStore
import com.yehjunb.nodopamine.data.InstalledApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 차단할 앱을 고르는 화면. 차단 중에는 이미 고른 앱을 뺄 수 없어요. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerScreen(state: DetoxState, onBack: () -> Unit) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    val apps by produceState<List<AppInfo>?>(null) {
        value = withContext(Dispatchers.IO) { InstalledApps.load(context, refresh = true) }
    }
    // 차단 중이면 지금 차단 목록에 있는 앱은 잠가요.
    val locked = remember(state.blockedPackages) {
        if (state.isBlocking(System.currentTimeMillis())) state.blockedPackages else emptySet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("차단할 앱 고르기") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("앱 이름 검색") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            if (locked.isNotEmpty()) {
                Text(
                    "차단 중이라 이미 고른 앱은 뺄 수 없어요. 새로 추가는 할 수 있어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            val list = apps
            if (list == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                // 이미 고른 앱을 위로
                val shown = list
                    .filter { query.isBlank() || it.label.contains(query, ignoreCase = true) }
                    .sortedByDescending { it.packageName in state.blockedPackages }
                LazyColumn(Modifier.fillMaxSize()) {
                    items(shown, key = { it.packageName }) { app ->
                        val checked = app.packageName in state.blockedPackages
                        val enabled = app.packageName !in locked
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = enabled) { toggle(state, app.packageName) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        ) {
                            Image(app.icon, contentDescription = null, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.width(14.dp))
                            Text(app.label, modifier = Modifier.weight(1f))
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { toggle(state, app.packageName) },
                                enabled = enabled,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun toggle(state: DetoxState, packageName: String) {
    val current = DetoxStore.state.value.blockedPackages
    DetoxStore.setBlockedPackages(if (packageName in current) current - packageName else current + packageName)
}
