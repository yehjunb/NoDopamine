package com.yehjunb.nodopamine.data

/**
 * 주소창을 읽을 수 있는 브라우저 목록.
 * 브라우저 앱 → 주소창 화면 요소의 ID. (브라우저 업데이트로 바뀔 수 있어요)
 * 여기에 없는 브라우저는 사이트 차단이 안 되니, 필요하면 "차단할 앱"에서 브라우저 자체를 막으세요.
 */
object Browsers {
    val addressBarIds: Map<String, List<String>> = mapOf(
        "com.android.chrome" to listOf("com.android.chrome:id/url_bar"),
        "com.chrome.beta" to listOf("com.chrome.beta:id/url_bar"),
        "com.sec.android.app.sbrowser" to listOf(
            "com.sec.android.app.sbrowser:id/location_bar_edit_text",
            "com.sec.android.app.sbrowser:id/custom_tab_toolbar_url_bar_text",
        ),
        "org.mozilla.firefox" to listOf("org.mozilla.firefox:id/mozac_browser_toolbar_url_view"),
        "com.microsoft.emmx" to listOf("com.microsoft.emmx:id/url_bar"),
        "com.brave.browser" to listOf("com.brave.browser:id/url_bar"),
        "com.naver.whale" to listOf("com.naver.whale:id/url_bar"),
    )
}
