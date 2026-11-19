package com.mgnegypt.previewmd.data.files

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

class RelativePathTest {

    @Test
    fun `plain file in the same folder`() {
        assertEquals(0 to listOf("pic.png"), parseRelativePath("pic.png"))
    }

    @Test
    fun `subfolder path`() {
        assertEquals(
            0 to listOf("images", "a b.png"),
            parseRelativePath("images/a%20b.png")
        )
    }

    @Test
    fun `parent steps counted`() {
        assertEquals(
            1 to listOf("shared", "x.png"),
            parseRelativePath("../shared/x.png")
        )
    }

    @Test
    fun `dot segments and mixed ups collapse`() {
        assertEquals(0 to listOf("b.png"), parseRelativePath("./sub/../b.png"))
    }

    @Test
    fun `absolute and scheme links are not relative`() {
        assertNull(parseRelativePath("https://example.com/a.png"))
        assertNull(parseRelativePath("/root/a.png"))
        assertNull(parseRelativePath("#anchor"))
        assertNull(parseRelativePath("content://x/a.png"))
    }

    @Test
    fun `finds only relative image links`() {
        val md = """
            ![a](images/a.png)
            ![b](https://example.com/b.png)
            ![c](../c.png)
            [not an image](other.md)
            ![a again](images/a.png)
        """.trimIndent()
        assertEquals(
            listOf("images/a.png", "../c.png"),
            findRelativeImageLinks(md)
        )
    }
}
