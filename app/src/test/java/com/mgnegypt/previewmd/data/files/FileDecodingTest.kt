package com.mgnegypt.previewmd.data.files

import org.junit.Assert.assertEquals
import org.junit.Test

class FileDecodingTest {

    @Test
    fun plainUtf8_roundTrips() {
        val text = "# Title\n\nSome body text."
        assertEquals(text, decodeUtf8(text.toByteArray(Charsets.UTF_8)))
    }

    @Test
    fun bom_isStripped() {
        val bytes = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) +
            "# Heading".toByteArray(Charsets.UTF_8)
        assertEquals("# Heading", decodeUtf8(bytes))
    }

    @Test
    fun arabicText_roundTrips() {
        val text = "# عنوان\n\nنص عربي هنا."
        assertEquals(text, decodeUtf8(text.toByteArray(Charsets.UTF_8)))
    }

    @Test
    fun malformedBytes_areReplacedNotThrown() {
        val bytes = byteArrayOf(0x41, 0xFF.toByte(), 0x42)
        val decoded = decodeUtf8(bytes)
        assertEquals(3, decoded.length)
        assertEquals('A', decoded[0])
        assertEquals('B', decoded[2])
    }
}
