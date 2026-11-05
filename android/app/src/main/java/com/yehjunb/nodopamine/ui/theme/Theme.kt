package com.yehjunb.nodopamine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** iOS 버전과 같은 초록색 */
val LeafGreen = Color(0xFF6BC28F)
private val LeafGreenDeep = Color(0xFF4A996B)

private val LightColors = lightColorScheme(
    primary = LeafGreenDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDEFE3),
    onPrimaryContainer = Color(0xFF0E2A1A),
    secondaryContainer = Color(0xFFDDEFE3),
    onSecondaryContainer = Color(0xFF0E2A1A),
)

private val DarkColors = darkColorScheme(
    primary = LeafGreen,
    onPrimary = Color(0xFF0E1A13),
    primaryContainer = Color(0xFF23352B),
    onPrimaryContainer = Color(0xFFDDEFE3),
    secondaryContainer = Color(0xFF23352B),
    onSecondaryContainer = Color(0xFFDDEFE3),
)

@Composable
fun NoDopamineTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
