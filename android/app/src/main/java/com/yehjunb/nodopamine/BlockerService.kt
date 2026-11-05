package com.yehjunb.nodopamine

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import com.yehjunb.nodopamine.data.Browsers
import com.yehjunb.nodopamine.data.DetoxStore
import com.yehjunb.nodopamine.data.Domains
import com.yehjunb.nodopamine.data.InstalledApps

/**
 * 차단의 핵심. 안드로이드 "접근성 서비스"로 동작해요.
 *
 * 화면에 새 앱이 뜰 때마다 안드로이드가 알려주면,
 * 1) 그 앱이 차단 목록에 있고 지금이 차단 시간이면 → 차단 화면을 띄워요.
 * 2) 브라우저라면 주소창을 읽어서 차단한 사이트인지 확인해요.
 *
 * 화면 내용을 저장하거나 밖으로 보내지 않아요. 앱 이름과 주소창만 확인해요.
 */
class BlockerService : AccessibilityService() {

    private var lastBlockAt = 0L
    private var lastAddressCheckAt = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        DetoxStore.init(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg == packageName) return // 우리 앱(차단 화면 포함)은 무시

        DetoxStore.checkCompletion()
        val state = DetoxStore.state.value
        if (!state.isBlocking(System.currentTimeMillis())) return

        // 1) 차단한 앱이 열렸을 때
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && pkg in state.blockedPackages) {
            showBlockScreen(InstalledApps.label(this, pkg))
            return
        }

        // 2) 브라우저에서 차단한 사이트를 열었을 때
        val addressBarIds = Browsers.addressBarIds[pkg] ?: return
        if (state.blockedDomains.isEmpty()) return
        val now = SystemClock.elapsedRealtime()
        if (now - lastAddressCheckAt < 400) return // 너무 자주 확인하지 않게
        lastAddressCheckAt = now

        val address = readAddressBar(addressBarIds) ?: return
        val matched = Domains.findMatch(address, state.blockedDomains) ?: return
        performGlobalAction(GLOBAL_ACTION_BACK) // 그 페이지에서 빠져나오기
        showBlockScreen(matched)
    }

    private fun readAddressBar(ids: List<String>): String? {
        val root = rootInActiveWindow ?: return null
        for (id in ids) {
            val text = root.findAccessibilityNodeInfosByViewId(id).firstOrNull()?.text?.toString()
            if (!text.isNullOrBlank()) return text
        }
        return null
    }

    private fun showBlockScreen(target: String) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastBlockAt < 800) return // 같은 순간에 여러 번 띄우지 않게
        lastBlockAt = now
        startActivity(
            Intent(this, BlockActivity::class.java)
                .putExtra(BlockActivity.EXTRA_TARGET, target)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        )
    }

    override fun onInterrupt() = Unit

    companion object {
        /** 사용자가 설정에서 이 서비스를 켰는지 확인 */
        fun isEnabled(context: Context): Boolean {
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            ) ?: return false
            val me = ComponentName(context, BlockerService::class.java)
            return enabled.split(':').any { ComponentName.unflattenFromString(it) == me }
        }
    }
}
