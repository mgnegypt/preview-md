package com.mgnegypt.previewmd.util

import org.junit.Assert.assertEquals
import org.junit.Test

class RelativeTimeTest {

    private val now = 10_000_000_000L
    private val minute = 60_000L
    private val hour = 60 * minute

    @Test
    fun underAMinute_isNow() {
        assertEquals(
            RelativeBucket.NOW,
            relativeTime(now, now - 30_000).bucket
        )
    }

    @Test
    fun minutes_areCounted() {
        val result = relativeTime(now, now - 5 * minute)
        assertEquals(RelativeBucket.MINUTES, result.bucket)
        assertEquals(5, result.count)
    }

    @Test
    fun hours_areCounted() {
        val result = relativeTime(now, now - 3 * hour)
        assertEquals(RelativeBucket.HOURS, result.bucket)
        assertEquals(3, result.count)
    }

    @Test
    fun between24And48Hours_isYesterday() {
        assertEquals(
            RelativeBucket.YESTERDAY,
            relativeTime(now, now - 30 * hour).bucket
        )
    }

    @Test
    fun days_areCounted() {
        val result = relativeTime(now, now - 72 * hour)
        assertEquals(RelativeBucket.DAYS, result.bucket)
        assertEquals(3, result.count)
    }

    @Test
    fun futureTimestamps_clampToNow() {
        assertEquals(
            RelativeBucket.NOW,
            relativeTime(now, now + hour).bucket
        )
    }
}
