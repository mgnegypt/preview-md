package com.mgnegypt.previewmd.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SemVerTest {

    @Test
    fun `parses plain and v-prefixed tags`() {
        assertEquals(SemVer(1, 2, 3), SemVer.parse("1.2.3"))
        assertEquals(SemVer(1, 2, 3), SemVer.parse("v1.2.3"))
        assertEquals(
            SemVer(2, 0, 0, listOf("beta", "1")),
            SemVer.parse("v2.0.0-beta.1")
        )
    }

    @Test
    fun `rejects malformed versions`() {
        assertNull(SemVer.parse("1.2"))
        assertNull(SemVer.parse("a.b.c"))
        assertNull(SemVer.parse("1.2.3-"))
        assertNull(SemVer.parse(""))
    }

    @Test
    fun `numeric ordering across components`() {
        assertTrue(SemVer.parse("1.10.0")!! > SemVer.parse("1.9.9")!!)
        assertTrue(SemVer.parse("2.0.0")!! > SemVer.parse("1.99.99")!!)
        assertTrue(SemVer.parse("1.0.1")!! > SemVer.parse("1.0.0")!!)
    }

    @Test
    fun `the semver org prerelease chain holds`() {
        val chain = listOf(
            "1.0.0-alpha",
            "1.0.0-alpha.1",
            "1.0.0-alpha.beta",
            "1.0.0-beta",
            "1.0.0-beta.2",
            "1.0.0-beta.11",
            "1.0.0-rc.1",
            "1.0.0"
        ).map { SemVer.parse(it)!! }
        for (i in 0 until chain.size - 1) {
            assertTrue(
                "${chain[i]} should be < ${chain[i + 1]}",
                chain[i] < chain[i + 1]
            )
        }
    }

    @Test
    fun `build metadata does not affect precedence`() {
        assertEquals(
            0,
            SemVer.parse("1.0.0+build.5")!!
                .compareTo(SemVer.parse("1.0.0+other")!!)
        )
    }
}
