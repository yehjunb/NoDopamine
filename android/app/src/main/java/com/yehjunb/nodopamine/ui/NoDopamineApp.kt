package com.yehjunb.nodopamine.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.yehjunb.nodopamine.data.DetoxStore

private data class Tab(val title: String, val icon: ImageVector)

private val TABS = listOf(
    Tab("디톡스", Icons.Filled.Spa),
    Tab("차단", Icons.Filled.Block),
    Tab("화면 단순화", Icons.Filled.Contrast),
)

/** 앱의 뼈대: 첫 안내 화면 → 탭 3개 (+ 앱 고르기, 웹사이트 화면) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoDopamineApp(serviceEnabled: Boolean) {
    val state by DetoxStore.state.collectAsState()
    var skippedOnboarding by rememberSaveable { mutableStateOf(false) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var subScreen by rememberSaveable { mutableStateOf<String?>(null) }

    if (!serviceEnabled && !skippedOnboarding) {
        OnboardingScreen(onSkip = { skippedOnboarding = true })
        return
    }

    when (subScreen) {
        "apps" -> {
            BackHandler { subScreen = null }
            AppPickerScreen(state, onBack = { subScreen = null })
            return
        }
        "websites" -> {
            BackHandler { subScreen = null }
            WebsiteScreen(state, onBack = { subScreen = null })
            return
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(TABS[tab].title) }) },
        bottomBar = {
            NavigationBar {
                TABS.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.title) },
                    )
                }
            }
        },
    ) { padding ->
        when (tab) {
            0 -> ChallengeScreen(state, padding)
            1 -> BlockScreen(
                state = state,
                serviceEnabled = serviceEnabled,
                contentPadding = padding,
                onPickApps = { subScreen = "apps" },
                onEditWebsites = { subScreen = "websites" },
            )
            else -> SimplifyScreen(state, padding)
        }
    }
}
