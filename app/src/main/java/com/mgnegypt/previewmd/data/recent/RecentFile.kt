package com.mgnegypt.previewmd.data.recent

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A file the user opened. [uri] is the content URI string; access survives
 * restarts because the app takes a persistable permission when the file is
 * picked. [treeUri] is set when the file came from a granted folder.
 */
@Entity(tableName = "recent_files")
data class RecentFile(
    @PrimaryKey val uri: String,
    val name: String,
    val folderLabel: String,
    val lastOpenedAt: Long,
    val treeUri: String? = null
)

/** A folder the user granted through the document tree picker. */
@Entity(tableName = "folder_roots")
data class FolderRoot(
    @PrimaryKey val uri: String,
    val name: String,
    val addedAt: Long
)
