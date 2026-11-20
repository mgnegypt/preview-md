package com.mgnegypt.previewmd.data.files

import org.junit.Assert.assertEquals
import org.junit.Test

/** The duplicate naming rule: free name wins, else "base (n).ext". */
class DuplicateNameTest {

    @Test
    fun freeName_isKept() {
        assertEquals(
            "notes.md",
            duplicateName(setOf("other.md"), "notes.md")
        )
    }

    @Test
    fun takenName_getsFirstNumber() {
        assertEquals(
            "README (1).md",
            duplicateName(setOf("README.md"), "README.md")
        )
    }

    @Test
    fun takenNumbers_areSkipped() {
        assertEquals(
            "README (3).md",
            duplicateName(
                setOf("README.md", "README (1).md", "README (2).md"),
                "README.md"
            )
        )
    }

    @Test
    fun noExtension_appendsNumberAtTheEnd() {
        assertEquals(
            "notes (1)",
            duplicateName(setOf("notes"), "notes")
        )
    }

    @Test
    fun multiDotName_splitsAtTheLastDot() {
        assertEquals(
            "archive.tar (1).gz",
            duplicateName(setOf("archive.tar.gz"), "archive.tar.gz")
        )
    }
}
