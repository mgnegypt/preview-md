package com.mgnegypt.previewmd.ui.editor

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.text.SpanStyle

/**
 * Lightweight Markdown syntax highlighting for the editor, applied as an
 * [OutputTransformation]: it only adds span styles over the buffer, the
 * text itself is never changed. Fenced code regions are found with a
 * line scan; inline patterns use bounded regexes. The editor disables
 * this above [HIGHLIGHT_LIMIT] characters (with a visible notice),
 * because restyling runs on every keystroke.
 */
class MarkdownHighlightTransformation(
    private val headingStyle: SpanStyle,
    private val codeStyle: SpanStyle,
    private val fenceStyle: SpanStyle,
    private val linkStyle: SpanStyle,
    private val markerStyle: SpanStyle
) : OutputTransformation {

    override fun TextFieldBuffer.transformOutput() {
        val text = asCharSequence().toString()
        if (text.isEmpty()) return

        val fences = fenceRanges(text)
        for ((fenceStart, fenceEnd) in fences) {
            addStyle(fenceStyle, fenceStart, fenceEnd)
        }

        fun styleOutsideFences(
            pattern: Regex,
            style: SpanStyle,
            group: Int = 0
        ) {
            for (match in pattern.findAll(text)) {
                val range = match.groups[group]?.range ?: match.range
                val insideFence = fences.any {
                    range.first >= it.first && range.last < it.second
                }
                if (!insideFence) {
                    addStyle(style, range.first, range.last + 1)
                }
            }
        }

        styleOutsideFences(HEADING, headingStyle)
        styleOutsideFences(BOLD, headingStyle)
        styleOutsideFences(INLINE_CODE, codeStyle)
        styleOutsideFences(LINK, linkStyle)
        styleOutsideFences(LIST_MARKER, markerStyle, group = 1)
        styleOutsideFences(QUOTE_MARKER, markerStyle, group = 1)
    }

    /** [start, end) ranges covered by fenced code blocks. */
    private fun fenceRanges(text: String): List<Pair<Int, Int>> {
        val ranges = mutableListOf<Pair<Int, Int>>()
        var inFence = false
        var fenceStart = 0
        var index = 0
        while (index <= text.length) {
            val lineEnd = text.indexOf('\n', index).let {
                if (it == -1) text.length else it
            }
            val line = text.substring(index, lineEnd)
            if (line.trimStart().startsWith("```")) {
                if (!inFence) {
                    inFence = true
                    fenceStart = index
                } else {
                    ranges.add(fenceStart to minOf(lineEnd + 1, text.length))
                    inFence = false
                }
            }
            if (lineEnd == text.length) break
            index = lineEnd + 1
        }
        if (inFence) {
            ranges.add(fenceStart to text.length)
        }
        return ranges
    }

    private companion object {
        val HEADING = Regex("""(?m)^#{1,6}\s+.*$""")
        val BOLD = Regex("""\*\*[^*\n]+\*\*|__[^_\n]+__""")
        val INLINE_CODE = Regex("""`[^`\n]+`""")
        val LINK = Regex("""\[[^\]\n]*\]\([^)\n]*\)""")
        val LIST_MARKER = Regex("""(?m)^(\s*(?:[-*+]|\d+\.)\s)""")
        val QUOTE_MARKER = Regex("""(?m)^(>\s?)""")
    }
}

/** Above this many characters the editor turns highlighting off. */
const val HIGHLIGHT_LIMIT = 300_000
