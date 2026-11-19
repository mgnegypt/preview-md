package com.mgnegypt.previewmd.data.recent

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeRecentDao : RecentFileDao {
    val items = MutableStateFlow<List<RecentFile>>(emptyList())

    private fun sorted(list: List<RecentFile>) =
        list.sortedByDescending { it.lastOpenedAt }

    override fun observeRecents(limit: Int): Flow<List<RecentFile>> =
        items.map { sorted(it).take(limit) }

    override fun searchRecents(
        query: String,
        limit: Int
    ): Flow<List<RecentFile>> =
        items.map { list ->
            sorted(list.filter { it.name.contains(query, ignoreCase = true) })
                .take(limit)
        }

    override suspend fun upsert(file: RecentFile) {
        items.value = items.value.filterNot { it.uri == file.uri } + file
    }

    override suspend fun deleteByUri(uri: String) {
        items.value = items.value.filterNot { it.uri == uri }
    }

    override suspend fun allUrisByRecency(): List<String> =
        sorted(items.value).map { it.uri }

    override suspend fun deleteByUris(uris: List<String>) {
        items.value = items.value.filterNot { it.uri in uris }
    }
}

private class FakeFolderDao : FolderRootDao {
    val items = MutableStateFlow<List<FolderRoot>>(emptyList())

    override fun observeRoots(): Flow<List<FolderRoot>> =
        items.map { list -> list.sortedBy { it.addedAt } }

    override suspend fun upsert(root: FolderRoot) {
        items.value = items.value.filterNot { it.uri == root.uri } + root
    }

    override suspend fun deleteByUri(uri: String) {
        items.value = items.value.filterNot { it.uri == uri }
    }
}

class RecentFilesRepositoryTest {

    private var now = 1_000_000L
    private lateinit var repository: RecentFilesRepository

    @Before
    fun setUp() {
        repository = RecentFilesRepository(
            recentDao = FakeRecentDao(),
            folderDao = FakeFolderDao(),
            nowMillis = { now }
        )
    }

    @Test
    fun recordOpened_ordersByRecency() = runTest {
        repository.recordOpened("uri://a", "a.md", "Docs")
        now += 1_000
        repository.recordOpened("uri://b", "b.md", "Docs")
        now += 1_000
        repository.recordOpened("uri://a", "a.md", "Docs")

        val recents = repository.recents.first()
        assertEquals(listOf("a.md", "b.md"), recents.map { it.name })
    }

    @Test
    fun recents_areCappedAtFifty() = runTest {
        repeat(55) { index ->
            now += 1_000
            repository.recordOpened("uri://$index", "f$index.md", "Docs")
        }
        val recents = repository.recents.first()
        assertEquals(RecentFilesRepository.MAX_RECENTS, recents.size)
        // The newest survive; the five oldest are gone.
        assertEquals("f54.md", recents.first().name)
        assertEquals("f5.md", recents.last().name)
    }

    @Test
    fun pruneInaccessible_removesOnlyDeadEntries() = runTest {
        repository.recordOpened("uri://alive", "alive.md", "Docs")
        repository.recordOpened("uri://dead", "dead.md", "Docs")

        repository.pruneInaccessible { it == "uri://alive" }

        val recents = repository.recents.first()
        assertEquals(listOf("alive.md"), recents.map { it.name })
    }

    @Test
    fun folders_addAndRemove() = runTest {
        repository.addFolder("tree://docs", "Docs")
        assertEquals(
            listOf("Docs"),
            repository.folderRoots.first().map { it.name }
        )
        repository.removeFolder("tree://docs")
        assertTrue(repository.folderRoots.first().isEmpty())
    }
}
