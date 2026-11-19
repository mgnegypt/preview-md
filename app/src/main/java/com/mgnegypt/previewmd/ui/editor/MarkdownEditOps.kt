package com.mgnegypt.previewmd.ui.editor

/** The result of a pure edit operation: new text and new selection. */
data class EditResult(
    val text: String,
    val selectionStart: Int,
    val selectionEnd: Int
)

/**
 * The editor toolbar operations as pure functions on (text, selection),
 * so every toggle behaviour is unit-tested without a UI. Each operation
 * is toggle-aware: applying it to already-formatted text removes the
 * formatting instead of doubling it.
 */
object MarkdownEditOps {

    /** Wraps the selection in [marker], or unwraps it when already wrapped. */
    fun toggleWrap(
        text: String,
        start: Int,
        end: Int,
        marker: String
    ): EditResult {
        val m = marker.length
        // Case 1: the markers sit immediately outside the selection.
        if (start >= m && end + m <= text.length &&
            text.substring(start - m, start) == marker &&
            text.substring(end, end + m) == marker
        ) {
            val newText = text.removeRange(end, end + m)
                .removeRange(start - m, start)
            return EditResult(newText, start - m, end - m)
        }
        // Case 2: the selection itself includes the markers.
        if (end - start >= 2 * m &&
            text.substring(start, start + m) == marker &&
            text.substring(end - m, end) == marker
        ) {
            val inner = text.substring(start + m, end - m)
            val newText = text.replaceRange(start, end, inner)
            return EditResult(newText, start, start + inner.length)
        }
        // Case 3: empty selection inserts a marker pair, cursor between.
        if (start == end) {
            val newText = text.replaceRange(start, start, marker + marker)
            return EditResult(newText, start + m, start + m)
        }
        // Case 4: wrap the selection, keeping it selected.
        val selected = text.substring(start, end)
        val newText = text.replaceRange(start, end, marker + selected + marker)
        return EditResult(newText, start + m, end + m)
    }

    /** Inline code: a single-line selection toggles backticks. */
    fun toggleInlineCode(text: String, start: Int, end: Int): EditResult =
        toggleWrap(text, start, end, "`")

    /**
     * Code block: wraps the touched lines in a ``` fence, or removes the
     * fence when the block is already fenced.
     */
    fun toggleCodeBlock(text: String, start: Int, end: Int): EditResult {
        var blockStart = lineStart(text, start)
        var blockEnd = lineEnd(text, end)
        // When the selection sits inside a fenced block, expand the
        // touched range to include the fence lines themselves.
        if (blockStart > 0 && blockEnd < text.length) {
            val aboveStart = lineStart(text, blockStart - 1)
            val above = text.substring(aboveStart, blockStart - 1)
            val belowEnd = lineEnd(text, blockEnd + 1)
            val below = text.substring(blockEnd + 1, belowEnd)
            if (above.trimStart().startsWith("```") && below.trim() == "```") {
                blockStart = aboveStart
                blockEnd = belowEnd
            }
        }
        val block = text.substring(blockStart, blockEnd)
        val lines = block.split("\n")
        val fenced = lines.size >= 2 &&
            lines.first().trimStart().startsWith("```") &&
            lines.last().trim() == "```"
        return if (fenced) {
            val inner = lines.subList(1, lines.size - 1).joinToString("\n")
            val newText = text.replaceRange(blockStart, blockEnd, inner)
            EditResult(newText, blockStart, blockStart + inner.length)
        } else {
            val fencedText = "```\n$block\n```"
            val newText = text.replaceRange(blockStart, blockEnd, fencedText)
            EditResult(newText, blockStart + 4, blockStart + 4 + block.length)
        }
    }

    /**
     * Toggles a heading of [level] on the first line touched by the
     * selection: same level removes it, another level replaces it, no
     * heading adds it.
     */
    fun toggleHeading(
        text: String,
        start: Int,
        end: Int,
        level: Int = 1
    ): EditResult {
        val ls = lineStart(text, start)
        val le = lineEnd(text, ls)
        val line = text.substring(ls, le)
        val existing = HEADING_PREFIX.find(line)
        val prefix = "#".repeat(level) + " "
        val newLine: String
        val delta: Int
        when {
            existing != null && existing.groupValues[1].length == level -> {
                newLine = line.substring(existing.value.length)
                delta = -existing.value.length
            }
            existing != null -> {
                newLine = prefix + line.substring(existing.value.length)
                delta = prefix.length - existing.value.length
            }
            else -> {
                newLine = prefix + line
                delta = prefix.length
            }
        }
        val newText = text.replaceRange(ls, le, newLine)
        val newStart = (start + delta).coerceIn(ls, ls + newLine.length)
        val newEnd = (end + delta).coerceIn(newStart, ls + newLine.length)
        return EditResult(newText, newStart, newEnd)
    }

    /**
     * Turns the selection into a link with the URL placeholder selected,
     * ready to type over. With no selection, inserts an empty link and
     * places the cursor in the text brackets.
     */
    fun insertLink(text: String, start: Int, end: Int): EditResult {
        val selected = text.substring(start, end)
        val inserted = "[$selected](url)"
        val newText = text.replaceRange(start, end, inserted)
        return if (selected.isEmpty()) {
            EditResult(newText, start + 1, start + 1)
        } else {
            val urlStart = start + 1 + selected.length + 2
            EditResult(newText, urlStart, urlStart + 3)
        }
    }

    /**
     * Toggles "- " bullets on every line touched by the selection: when
     * all non-blank touched lines are bulleted they are un-bulleted,
     * otherwise all get bulleted.
     */
    fun toggleList(text: String, start: Int, end: Int): EditResult {
        val blockStart = lineStart(text, start)
        val blockEnd = lineEnd(text, end)
        val block = text.substring(blockStart, blockEnd)
        val lines = block.split("\n")
        val contentLines = lines.filter { it.isNotBlank() }
        val allBulleted = contentLines.isNotEmpty() &&
            contentLines.all { it.trimStart().startsWith("- ") }
        val newLines = lines.map { line ->
            when {
                line.isBlank() -> line
                allBulleted -> line.replaceFirst(Regex("""^(\s*)- """), "$1")
                line.trimStart().startsWith("- ") -> line
                else -> {
                    val indent = line.takeWhile { it == ' ' || it == '\t' }
                    "$indent- ${line.substring(indent.length)}"
                }
            }
        }
        val newBlock = newLines.joinToString("\n")
        val newText = text.replaceRange(blockStart, blockEnd, newBlock)
        return EditResult(newText, blockStart, blockStart + newBlock.length)
    }

    private val HEADING_PREFIX = Regex("""^(#{1,6})\s+""")

    private fun lineStart(text: String, index: Int): Int {
        if (index <= 0) return 0
        return text.lastIndexOf('\n', index - 1) + 1
    }

    private fun lineEnd(text: String, index: Int): Int {
        val found = text.indexOf('\n', index)
        return if (found == -1) text.length else found
    }
}
