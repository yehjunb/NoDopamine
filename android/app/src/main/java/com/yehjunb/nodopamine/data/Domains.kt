package com.yehjunb.nodopamine.data

/** 웹사이트 주소를 다루는 도우미 (순수 계산이라 단위 테스트 가능) */
object Domains {
    const val MAX = 50

    /** "https://www.YouTube.com/watch?v=..." → "youtube.com". 주소가 아니면 null. */
    fun normalize(raw: String): String? {
        var s = raw.trim().lowercase()
        val scheme = s.indexOf("://")
        if (scheme >= 0) s = s.substring(scheme + 3)
        val cut = s.indexOfFirst { it in "/?#:" }
        if (cut >= 0) s = s.substring(0, cut)
        s = s.removePrefix("www.")
        if (!s.contains('.') || s.startsWith('.') || s.endsWith('.')) return null
        if (!s.all { it in 'a'..'z' || it in '0'..'9' || it == '.' || it == '-' }) return null
        return s
    }

    /**
     * 브라우저 주소창의 주소가 차단 목록에 걸리면 걸린 도메인을, 아니면 null 을 돌려줘요.
     * "m.youtube.com" 은 "youtube.com" 에 걸려요. "notyoutube.com" 은 안 걸려요.
     */
    fun findMatch(addressBarText: String, blocked: List<String>): String? {
        val host = normalize(addressBarText) ?: return null
        return blocked.firstOrNull { host == it || host.endsWith(".$it") }
    }
}
