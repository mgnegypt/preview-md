package com.mgnegypt.previewmd.ui.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownEditOpsTest {

    @Test
    fun `wrap adds markers and keeps the word selected`() {
        val r = MarkdownEditOps.toggleWrap("hello world", 6, 11, "**")
        assertEquals("hello **world**", r.text)
        assertEquals(8, r.selectionStart)
        assertEquals(13, r.selectionEnd)
    }

    @Test
    fun `wrap again unwraps`() {
        val r = MarkdownEditOps.toggleWrap("hello **world**", 8, 13, "**")
        assertEquals("hello world", r.text)
        assertEquals(6, r.selectionStart)
        assertEquals(11, r.selectionEnd)
    }

    @Test
    fun `unwrap when selection includes the markers`() {
        val r = MarkdownEditOps.toggleWrap("**b**", 0, 5, "*")
        assertEquals("*b*", r.text)
        assertEquals(0, r.selectionStart)
        assertEquals(3, r.selectionEnd)
    }

    @Test
    fun `empty selection inserts a marker pair with cursor between`() {
        val r = MarkdownEditOps.toggleWrap("ab", 1, 1, "**")
        assertEquals("a****b", r.text)
        assertEquals(3, r.selectionStart)
        assertEquals(3, r.selectionEnd)
    }

    @Test
    fun `heading toggles on and off`() {
        val on = MarkdownEditOps.toggleHeading("Title\nbody", 0, 5)
        assertEquals("# Title\nbody", on.text)
        val off = MarkdownEditOps.toggleHeading(on.text, 2, 7)
        assertEquals("Title\nbody", off.text)
    }

    @Test
    fun `heading level is replaced not stacked`() {
        val r = MarkdownEditOps.toggleHeading("## Title", 3, 8, level = 1)
        assertEquals("# Title", r.text)
    }

    @Test
    fun `link selects the url placeholder`() {
        val r = MarkdownEditOps.insertLink("see docs here", 4, 8)
        assertEquals("see [docs](url) here", r.text)
        assertEquals("url", r.text.substring(r.selectionStart, r.selectionEnd))
    }

    @Test
    fun `list toggles bullets on touched lines`() {
        val on = MarkdownEditOps.toggleList("one\ntwo", 0, 7)
        assertEquals("- one\n- two", on.text)
        val off = MarkdownEditOps.toggleList(on.text, 0, on.text.length)
        assertEquals("one\ntwo", off.text)
    }

    @Test
    fun `code block fences and unfences`() {
        val on = MarkdownEditOps.toggleCodeBlock("val x = 1", 0, 9)
        assertEquals("```\nval x = 1\n```", on.text)
        val off = MarkdownEditOps.toggleCodeBlock(on.text, 4, 13)
        assertEquals("val x = 1", off.text)
    }

    @Test
    fun `inline code toggles backticks`() {
        val on = MarkdownEditOps.toggleInlineCode("use copy here", 4, 8)
        assertEquals("use `copy` here", on.text)
        val off = MarkdownEditOps.toggleInlineCode(on.text, 5, 9)
        assertEquals("use copy here", off.text)
    }
}
