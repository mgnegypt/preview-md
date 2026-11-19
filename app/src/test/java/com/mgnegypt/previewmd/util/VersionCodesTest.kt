package com.mgnegypt.previewmd.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionCodesTest {
    @Test
    fun `plan examples`() {
        assertEquals(1_000_099, VersionCodes.of(1, 0, 0))
        assertEquals(1_010_001, VersionCodes.of(1, 1, 0, beta = 1))
        assertEquals(1_010_099, VersionCodes.of(1, 1, 0))
    }

    @Test
    fun `stable sorts after every beta of the same version`() {
        assertTrue(VersionCodes.of(1, 1, 0, beta = 98) < VersionCodes.of(1, 1, 0))
        assertTrue(VersionCodes.of(1, 1, 0, beta = 1) < VersionCodes.of(1, 1, 0, beta = 2))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `minor above 99 is rejected`() {
        VersionCodes.of(1, 100, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `beta above 98 is rejected`() {
        VersionCodes.of(1, 0, 0, beta = 99)
    }
}
