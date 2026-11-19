package com.mgnegypt.previewmd.data.files

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class FileErrorKind {
    /** The persisted permission is gone or the provider denies access. */
    PERMISSION_LOST,

    /** The document no longer exists. */
    NOT_FOUND,

    /** Any other I/O failure. */
    IO_FAILURE
}

class FileAccessException(
    val kind: FileErrorKind,
    cause: Throwable? = null
) : Exception(cause?.message, cause)

/** A Markdown file found inside a granted folder tree. */
data class TreeChild(
    val uri: Uri,
    val name: String,
    val lastModified: Long
)

/**
 * All content-URI file access (SAF). Reads and writes are UTF-8; decoding
 * never fails on malformed bytes (they are replaced), and every failure is
 * surfaced as a [FileAccessException] with a friendly [FileErrorKind].
 */
class FileRepository(context: Context) {

    private val appContext = context.applicationContext
    private val resolver get() = appContext.contentResolver

    suspend fun readText(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw FileAccessException(FileErrorKind.NOT_FOUND)
            decodeUtf8(bytes)
        } catch (e: FileAccessException) {
            throw e
        } catch (e: SecurityException) {
            throw FileAccessException(FileErrorKind.PERMISSION_LOST, e)
        } catch (e: FileNotFoundException) {
            throw FileAccessException(FileErrorKind.NOT_FOUND, e)
        } catch (e: Exception) {
            throw FileAccessException(FileErrorKind.IO_FAILURE, e)
        }
    }

    suspend fun writeText(uri: Uri, text: String) = withContext(Dispatchers.IO) {
        try {
            resolver.openOutputStream(uri, "wt")?.use {
                it.write(text.toByteArray(Charsets.UTF_8))
            } ?: throw FileAccessException(FileErrorKind.NOT_FOUND)
        } catch (e: FileAccessException) {
            throw e
        } catch (e: SecurityException) {
            throw FileAccessException(FileErrorKind.PERMISSION_LOST, e)
        } catch (e: FileNotFoundException) {
            throw FileAccessException(FileErrorKind.NOT_FOUND, e)
        } catch (e: Exception) {
            throw FileAccessException(FileErrorKind.IO_FAILURE, e)
        }
    }

    /** The document's display name, with sensible fallbacks. */
    suspend fun displayName(uri: Uri): String = withContext(Dispatchers.IO) {
        queryString(uri, OpenableColumns.DISPLAY_NAME)?.takeIf { it.isNotBlank() }
            ?: documentIdName(uri)
            ?: uri.lastPathSegment?.substringAfterLast('/')
            ?: "file.md"
    }

    /**
     * A short label for the folder containing the document, derived from
     * its document id (for example "primary:Docs/notes.md" -> "Docs").
     * Empty when it cannot be determined.
     */
    suspend fun parentLabel(uri: Uri): String = withContext(Dispatchers.IO) {
        val id = runCatching {
            if (DocumentsContract.isDocumentUri(appContext, uri)) {
                DocumentsContract.getDocumentId(uri)
            } else {
                null
            }
        }.getOrNull() ?: return@withContext ""
        val path = id.substringAfter(':', "")
        val parent = path.substringBeforeLast('/', "")
        parent.substringAfterLast('/')
    }

    /** True when the document can still be queried (permission alive). */
    suspend fun isAccessible(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            resolver.query(
                uri,
                arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID),
                null,
                null,
                null
            )?.use { it.moveToFirst() } ?: false
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    /** Lists the Markdown files directly inside a granted folder tree. */
    suspend fun listMarkdownFiles(treeUri: Uri): List<TreeChild> =
        withContext(Dispatchers.IO) {
            val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                treeUri,
                treeDocId
            )
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED
            )
            val result = mutableListOf<TreeChild>()
            try {
                resolver.query(childrenUri, projection, null, null, null)
                    ?.use { cursor ->
                        while (cursor.moveToNext()) {
                            val docId = cursor.getString(0) ?: continue
                            val name = cursor.getString(1) ?: continue
                            val mime = cursor.getString(2)
                            if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                                continue
                            }
                            if (!isMarkdown(name, mime)) continue
                            result += TreeChild(
                                uri = DocumentsContract.buildDocumentUriUsingTree(
                                    treeUri,
                                    docId
                                ),
                                name = name,
                                lastModified = if (cursor.isNull(3)) {
                                    0L
                                } else {
                                    cursor.getLong(3)
                                }
                            )
                        }
                    }
            } catch (e: SecurityException) {
                throw FileAccessException(FileErrorKind.PERMISSION_LOST, e)
            }
            result.sortedBy { it.name.lowercase() }
        }

    private fun isMarkdown(name: String, mime: String?): Boolean {
        val lower = name.lowercase()
        return mime == "text/markdown" ||
            mime == "text/x-markdown" ||
            lower.endsWith(".md") ||
            lower.endsWith(".markdown")
    }

    private fun documentIdName(uri: Uri): String? = runCatching {
        if (DocumentsContract.isDocumentUri(appContext, uri)) {
            DocumentsContract.getDocumentId(uri)
                .substringAfterLast('/')
                .takeIf { it.isNotBlank() }
        } else {
            null
        }
    }.getOrNull()

    private fun queryString(uri: Uri, column: String): String? = try {
        resolver.query(uri, arrayOf(column), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Decodes UTF-8 bytes, stripping a leading BOM and replacing malformed
 * sequences instead of failing. Pure and unit-tested.
 */
internal fun decodeUtf8(bytes: ByteArray): String {
    val hasBom = bytes.size >= 3 &&
        bytes[0] == 0xEF.toByte() &&
        bytes[1] == 0xBB.toByte() &&
        bytes[2] == 0xBF.toByte()
    val start = if (hasBom) 3 else 0
    val decoder = Charsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE)
    return decoder.decode(
        ByteBuffer.wrap(bytes, start, bytes.size - start)
    ).toString()
}
