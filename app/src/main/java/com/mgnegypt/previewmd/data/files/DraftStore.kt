package com.mgnegypt.previewmd.data.files

import android.content.Context
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Crash-safe editor drafts in app-private storage, one file per document
 * URI. A draft is written while editing, restored when the editor is
 * reopened after a process death, and deleted on save or discard.
 */
class DraftStore(context: Context) {

    private val draftsDir = File(
        context.applicationContext.filesDir,
        "drafts"
    )

    private fun fileFor(uriString: String): File {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(uriString.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return File(draftsDir, "$digest.draft")
    }

    suspend fun read(uriString: String): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = fileFor(uriString)
                if (file.exists()) file.readText(Charsets.UTF_8) else null
            }.getOrNull()
        }

    suspend fun write(uriString: String, text: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                draftsDir.mkdirs()
                fileFor(uriString).writeText(text, Charsets.UTF_8)
            }
        }
    }

    suspend fun delete(uriString: String) {
        withContext(Dispatchers.IO) {
            runCatching { fileFor(uriString).delete() }
        }
    }
}
