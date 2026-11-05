package com.yehjunb.nodopamine.data

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap,
)

/** 폰에 설치된 앱 목록 (홈 화면에 아이콘이 있는 앱만). 한 번 읽으면 기억해 둬요. */
object InstalledApps {
    @Volatile
    private var cache: List<AppInfo>? = null

    /** 시간이 걸리니 백그라운드에서 불러주세요. */
    @Suppress("DEPRECATION")
    fun load(context: Context, refresh: Boolean = false): List<AppInfo> {
        if (!refresh) cache?.let { return it }
        val pm = context.packageManager
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(launcher, 0)
            .map { it.activityInfo.packageName }
            .distinct()
            .filter { it != context.packageName }
            .mapNotNull { pkg ->
                runCatching {
                    val info = pm.getApplicationInfo(pkg, 0)
                    AppInfo(
                        packageName = pkg,
                        label = pm.getApplicationLabel(info).toString(),
                        icon = pm.getApplicationIcon(info).toBitmap(96, 96).asImageBitmap(),
                    )
                }.getOrNull()
            }
            .sortedBy { it.label.lowercase() }
        cache = apps
        return apps
    }

    /** 패키지 이름 → 앱 이름 (예: com.google.android.youtube → YouTube) */
    @Suppress("DEPRECATION")
    fun label(context: Context, packageName: String): String = runCatching {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    }.getOrDefault(packageName)
}
