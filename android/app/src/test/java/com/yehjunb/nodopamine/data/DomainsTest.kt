package com.yehjunb.nodopamine.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DomainsTest {

    @Test
    fun `주소를 깔끔한 도메인으로 바꾼다`() {
        assertEquals("youtube.com", Domains.normalize("https://www.YouTube.com/watch?v=abc"))
        assertEquals("youtube.com", Domains.normalize("  youtube.com  "))
        assertEquals("m.youtube.com", Domains.normalize("m.youtube.com/shorts"))
        assertEquals("namu.wiki", Domains.normalize("namu.wiki"))
        assertEquals("example.com", Domains.normalize("http://example.com:8080/path"))
    }

    @Test
    fun `주소가 아니면 null`() {
        assertNull(Domains.normalize("youtube"))
        assertNull(Domains.normalize("funny cat videos"))
        assertNull(Domains.normalize(".com"))
        assertNull(Domains.normalize(""))
    }

    @Test
    fun `하위 주소도 차단되고 비슷한 이름은 차단되지 않는다`() {
        val blocked = listOf("youtube.com", "instagram.com")
        assertEquals("youtube.com", Domains.findMatch("youtube.com/watch?v=1", blocked))
        assertEquals("youtube.com", Domains.findMatch("https://m.youtube.com", blocked))
        assertEquals("instagram.com", Domains.findMatch("www.instagram.com/reels", blocked))
        assertNull(Domains.findMatch("notyoutube.com", blocked))
        assertNull(Domains.findMatch("google.com/search?q=youtube.com", blocked))
        assertNull(Domains.findMatch("유튜브 검색", blocked))
    }
}
