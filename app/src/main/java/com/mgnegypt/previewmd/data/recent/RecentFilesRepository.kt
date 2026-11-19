package com.mgnegypt.previewmd.data.recent

import kotlinx.coroutines.flow.Flow

/**
 * Recents and granted folders. Recents are ordered by last opened time and
 * capped at [MAX_RECENTS]; entries whose permission was revoked are pruned
 * by [pruneInaccessible] using a caller-supplied access check, which keeps
 * this class free of Android APIs and unit-testable.
 */
class RecentFilesRepository(
    private val recentDao: RecentFileDao,
    private val folderDao: FolderRootDao,
    private val nowMillis: () -> Long = { System.currentTimeMillis() }
) {

    val recents: Flow<List<RecentFile>> = recentDao.observeRecents(MAX_RECENTS)

    val folderRoots: Flow<List<FolderRoot>> = folderDao.observeRoots()

    fun searchRecents(query: String): Flow<List<RecentFile>> =
        if (query.isBlank()) {
            recents
        } else {
            recentDao.searchRecents(query.trim(), MAX_RECENTS)
        }

    suspend fun recordOpened(
        uri: String,
        name: String,
        folderLabel: String,
        treeUri: String? = null
    ) {
        recentDao.upsert(
            RecentFile(
                uri = uri,
                name = name,
                folderLabel = folderLabel,
                lastOpenedAt = nowMillis(),
                treeUri = treeUri
            )
        )
        trimToCap()
    }

    suspend fun removeRecent(uri: String) {
        recentDao.deleteByUri(uri)
    }

    /** Drops recents the access check can no longer open. */
    suspend fun pruneInaccessible(isAccessible: (String) -> Boolean) {
        val uris = recentDao.allUrisByRecency()
        val dead = uris.filter { !isAccessible(it) }
        if (dead.isNotEmpty()) {
            recentDao.deleteByUris(dead)
        }
    }

    suspend fun addFolder(uri: String, name: String) {
        folderDao.upsert(
            FolderRoot(uri = uri, name = name, addedAt = nowMillis())
        )
    }

    suspend fun removeFolder(uri: String) {
        folderDao.deleteByUri(uri)
    }

    private suspend fun trimToCap() {
        val uris = recentDao.allUrisByRecency()
        if (uris.size > MAX_RECENTS) {
            recentDao.deleteByUris(uris.drop(MAX_RECENTS))
        }
    }

    companion object {
        const val MAX_RECENTS = 50
    }
}
