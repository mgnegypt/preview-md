package com.mgnegypt.previewmd.data.files

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
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

    /**
     * Takes a persistable read+write permission for a picked document or
     * tree, so access survives process death and restarts. Fails silently
     * for grants that cannot be persisted (for example some VIEW intents);
     * the temporary grant still covers the current session.
     */
    fun takePersistablePermission(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching { resolver.takePersistableUriPermission(uri, flags) }
    }

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

    /** The display name of a granted folder tree root. */
    suspend fun treeDisplayName(treeUri: Uri): String = withContext(Dispatchers.IO) {
        runCatching {
            DocumentFile.fromTreeUri(appContext, treeUri)?.name
        }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: treeUri.lastPathSegment?.substringAfterLast('/')
            ?: "Folder"
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

    /**
     * Resolves a relative link (image or document) against the folder
     * that contains [documentUri], walking up for ".." segments and
     * down by child name through the provider. Returns the target's
     * document URI string, or null when any step is unreachable.
     * Best-effort by provider design; callers degrade gracefully.
     */
    suspend fun resolveSibling(
        documentUri: Uri,
        relativePath: String
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val parsed = parseRelativePath(relativePath) ?: return@runCatching null
            val (ups, downs) = parsed
            if (downs.isEmpty()) return@runCatching null
            var current: DocumentFile? = parentFolderOf(documentUri)
            repeat(ups) {
                current = current?.parentFile
            }
            for (segment in downs) {
                current = current?.listFiles()
                    ?.firstOrNull { it.name == segment }
                if (current == null) return@runCatching null
            }
            val target = current ?: return@runCatching null
            if (target.isFile) target.uri.toString() else null
        }.getOrNull()
    }

    /** Deletes the document itself. False when the provider refuses. */
    suspend fun deleteDocument(uri: Uri): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                DocumentsContract.deleteDocument(resolver, uri)
            }.getOrDefault(false)
        }

    /**
     * Duplicates [uri] beside itself, as "name (1).ext" (first free
     * number). Null when the parent folder is not reachable or not
     * writable through the grants the app holds.
     */
    suspend fun duplicateBeside(uri: Uri): Uri? =
        withContext(Dispatchers.IO) {
            runCatching {
                val parent = parentFolderOf(uri)
                    ?: return@runCatching null
                duplicateInto(parent, uri)
            }.getOrNull()
        }

    /** Duplicates [uri] into [folderUri], keeping its name if free. */
    suspend fun duplicateIntoFolder(uri: Uri, folderUri: Uri): Uri? =
        withContext(Dispatchers.IO) {
            runCatching {
                val folder = folderDocument(folderUri)
                    ?: return@runCatching null
                duplicateInto(folder, uri)
            }.getOrNull()
        }

    private fun duplicateInto(parent: DocumentFile, uri: Uri): Uri? {
        if (!parent.canWrite()) return null
        val source = DocumentFile.fromSingleUri(appContext, uri) ?: return null
        val name = source.name ?: return null
        val existing = parent.listFiles().mapNotNull { it.name }.toSet()
        val created = parent.createFile(
            resolver.getType(uri) ?: "text/markdown",
            duplicateName(existing, name)
        ) ?: return null
        return if (copyBytes(uri, created.uri)) {
            created.uri
        } else {
            runCatching {
                DocumentsContract.deleteDocument(resolver, created.uri)
            }
            null
        }
    }

    private fun copyBytes(from: Uri, to: Uri): Boolean = runCatching {
        val input = resolver.openInputStream(from) ?: return false
        val output = resolver.openOutputStream(to, "wt") ?: return false
        input.use { i -> output.use { o -> i.copyTo(o) } }
        true
    }.getOrDefault(false)

    /**
     * A cheap change fingerprint for a document: (size, lastModified).
     * Null when the provider cannot supply one or the file is gone.
     */
    fun documentFingerprint(uri: Uri): Pair<Long, Long>? = try {
        val file = DocumentFile.fromSingleUri(appContext, uri)
        if (file != null && file.exists()) {
            file.length() to file.lastModified()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }

    private fun queryString(uri: Uri, column: String): String? = try {
        resolver.query(uri, arrayOf(column), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    } catch (e: Exception) {
        null
    }

    // ---- the app folder -----------------------------------------------
    //
    // The app keeps its own folder, "Preview MD", inside a tree the user
    // granted once (Download). New files are born there, and saving a
    // file that lives anywhere else writes the edited copy there instead
    // of touching the original.

    /**
     * Finds the app folder inside [treeUri], creating it when missing.
     * Returns the folder's document URI, or null when the tree is not
     * reachable.
     */
    /**
     * The app folder for a granted tree. Android forbids granting
     * Download itself, so the one-time setup normally grants the
     * "Preview MD" folder inside Download directly — a tree that IS
     * the app folder is used as-is. For any other granted tree, the
     * folder is found or created inside it. Null when a non-folder
     * with that name exists or the tree is unreachable.
     */
    suspend fun appFolderDocument(treeUri: Uri): Uri? =
        withContext(Dispatchers.IO) {
            runCatching {
                val root = DocumentFile.fromTreeUri(appContext, treeUri)
                    ?: return@runCatching null
                if (!root.exists() || !root.isDirectory) return@runCatching null
                if (root.name == APP_FOLDER_NAME) return@runCatching root.uri
                val existing = root.findFile(APP_FOLDER_NAME)
                when {
                    existing != null && existing.isDirectory -> existing.uri
                    existing != null -> null
                    else -> root.createDirectory(APP_FOLDER_NAME)?.uri
                }
            }.getOrNull()
        }

    /** The document URI of the child of [folderUri] named [name], if any. */
    suspend fun findInFolder(folderUri: Uri, name: String): Uri? =
        withContext(Dispatchers.IO) {
            runCatching {
                folderDocument(folderUri)?.findFile(name)?.uri
            }.getOrNull()
        }

    /** Creates a Markdown file named [name] inside [folderUri]. */
    suspend fun createInFolder(folderUri: Uri, name: String): Uri? =
        withContext(Dispatchers.IO) {
            runCatching {
                folderDocument(folderUri)
                    ?.createFile("text/markdown", name)?.uri
            }.getOrNull()
        }

    /**
     * The [DocumentFile] for a folder. Folder URIs in this app carry
     * the tree segment (they come from [appFolderDocument] or from
     * documents inside that tree), and only a tree-backed
     * DocumentFile can list or create children:
     * DocumentFile.fromSingleUri wraps the URI in a
     * SingleDocumentFile, whose createFile throws
     * UnsupportedOperationException and whose findFile finds
     * nothing, so every folder write through it silently failed.
     */
    private fun folderDocument(folderUri: Uri): DocumentFile? =
        if (DocumentsContract.isTreeUri(folderUri)) {
            DocumentFile.fromTreeUri(appContext, folderUri)
        } else {
            DocumentFile.fromSingleUri(appContext, folderUri)
        }

    /**
     * The folder containing [uri], through the tree grant the URI
     * itself carries. Null for URIs without a tree segment (a file
     * picked on its own grants access to that file only, never to
     * its folder) and for the tree root itself.
     */
    private fun parentFolderOf(uri: Uri): DocumentFile? {
        if (!DocumentsContract.isTreeUri(uri)) return null
        val documentId = runCatching {
            DocumentsContract.getDocumentId(uri)
        }.getOrNull() ?: return null
        val treeId = runCatching {
            DocumentsContract.getTreeDocumentId(uri)
        }.getOrNull() ?: return null
        val parentId = parentDocumentId(documentId, treeId) ?: return null
        val authority = uri.authority ?: return null
        val parentTreeUri = DocumentsContract.buildTreeDocumentUri(
            authority,
            parentId
        )
        return DocumentFile.fromTreeUri(appContext, parentTreeUri)
    }

    /**
     * True when [fileUri] is a document directly or transitively inside
     * [folderUri]. Both must come from the same provider; the comparison
     * is on document ids, so it works for single-document and tree URIs
     * alike.
     */
    fun isInsideFolder(fileUri: Uri, folderUri: Uri): Boolean {
        if (fileUri.authority != folderUri.authority) return false
        val fileId = runCatching {
            DocumentsContract.getDocumentId(fileUri)
        }.getOrNull() ?: return false
        val folderId = runCatching {
            DocumentsContract.getDocumentId(folderUri)
        }.getOrNull() ?: return false
        return isDocumentInsideFolder(fileId, folderId)
    }

    companion object {
        const val APP_FOLDER_NAME = "Preview MD"
    }
}

/** Pure document-id check behind [FileRepository.isInsideFolder]. */
fun isDocumentInsideFolder(fileDocumentId: String, folderDocumentId: String): Boolean =
    fileDocumentId.startsWith("$folderDocumentId/")

/**
 * The parent document id of [documentId] inside the tree rooted at
 * [treeDocumentId]: the id with its last segment removed. Null when
 * [documentId] is the tree root itself or lies outside the tree —
 * the app holds no grant above the root it was given. Pure and
 * unit-tested.
 */
fun parentDocumentId(documentId: String, treeDocumentId: String): String? {
    if (documentId == treeDocumentId) return null
    if (!documentId.startsWith("$treeDocumentId/")) return null
    val parent = documentId.substringBeforeLast('/')
    return parent.ifEmpty { null }
}

/**
 * The name for a duplicate of [name] inside a folder whose current
 * names are [existing]: the name itself when free, otherwise
 * "base (1).ext", "base (2).ext", ... Pure and unit-tested.
 */
fun duplicateName(existing: Set<String>, name: String): String {
    if (name !in existing) return name
    val dot = name.lastIndexOf('.')
    val base = if (dot > 0) name.substring(0, dot) else name
    val extension = if (dot > 0) name.substring(dot) else ""
    var number = 1
    while (true) {
        val candidate = "$base ($number)$extension"
        if (candidate !in existing) return candidate
        number++
    }
}

/**
 * Splits a relative Markdown link into the number of ".." steps above
 * the containing folder and the remaining downward path segments
 * (URL-decoded). Returns null for absolute or scheme-carrying links.
 * Pure and unit-tested.
 */
fun parseRelativePath(link: String): Pair<Int, List<String>>? {
    val clean = link.trim()
    if (clean.isEmpty() || clean.startsWith("/") ||
        clean.contains("://") || clean.startsWith("#")
    ) {
        return null
    }
    var ups = 0
    val downs = mutableListOf<String>()
    for (raw in clean.replace('\\', '/').split('/')) {
        when (raw) {
            "", "." -> Unit
            ".." -> if (downs.isEmpty()) ups++ else downs.removeAt(downs.lastIndex)
            else -> downs.add(percentDecode(raw))
        }
    }
    return ups to downs
}

/**
 * Decodes %XX escapes as UTF-8 without touching android.net.Uri, so the
 * path helpers stay pure and unit-testable on the JVM. A '%' that is not
 * followed by two hex digits is kept literally.
 */
internal fun percentDecode(value: String): String {
    if (!value.contains('%')) return value
    val bytes = mutableListOf<Byte>()
    val out = StringBuilder()
    fun flushBytes() {
        if (bytes.isNotEmpty()) {
            out.append(String(bytes.toByteArray(), Charsets.UTF_8))
            bytes.clear()
        }
    }
    var i = 0
    while (i < value.length) {
        val c = value[i]
        val hex = if (c == '%' && i + 2 <= value.lastIndex) {
            value.substring(i + 1, i + 3).toIntOrNull(16)
        } else {
            null
        }
        if (hex != null) {
            bytes.add(hex.toByte())
            i += 3
        } else {
            flushBytes()
            out.append(c)
            i++
        }
    }
    flushBytes()
    return out.toString()
}

/**
 * Finds the distinct relative image links in Markdown source: inline
 * images whose target carries no scheme. Pure and unit-tested.
 */
fun findRelativeImageLinks(markdown: String): List<String> {
    val pattern = Regex("""!\[[^\]]*\]\(\s*([^\s)]+)""")
    return pattern.findAll(markdown)
        .map { it.groupValues[1] }
        .filter { parseRelativePath(it) != null }
        .distinct()
        .toList()
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
