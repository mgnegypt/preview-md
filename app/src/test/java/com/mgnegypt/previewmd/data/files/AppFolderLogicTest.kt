package com.mgnegypt.previewmd.data.files

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The document-id rule behind the app folder: a file counts as inside
 * the folder only when its document id is a strict descendant of the
 * folder's document id.
 */
class AppFolderLogicTest {

    private val folder = "primary:Download/Preview MD"

    @Test
    fun directChild_isInside() {
        assertTrue(
            isDocumentInsideFolder("primary:Download/Preview MD/notes.md", folder)
        )
    }

    @Test
    fun nestedChild_isInside() {
        assertTrue(
            isDocumentInsideFolder(
                "primary:Download/Preview MD/sub/notes.md",
                folder
            )
        )
    }

    @Test
    fun theFolderItself_isNotInside() {
        assertFalse(isDocumentInsideFolder(folder, folder))
    }

    @Test
    fun siblingFolder_isNotInside() {
        assertFalse(
            isDocumentInsideFolder("primary:Download/Other/notes.md", folder)
        )
    }

    @Test
    fun namePrefixLookalike_isNotInside() {
        // "Preview MD2" starts with the same text but is not the folder.
        assertFalse(
            isDocumentInsideFolder(
                "primary:Download/Preview MD2/notes.md",
                folder
            )
        )
    }

    @Test
    fun parentFolder_isNotInside() {
        assertFalse(
            isDocumentInsideFolder("primary:Download/notes.md", folder)
        )
    }
}
