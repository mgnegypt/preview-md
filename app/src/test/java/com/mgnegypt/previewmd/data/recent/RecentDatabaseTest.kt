package com.mgnegypt.previewmd.data.recent

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The repository against a REAL Room database (in-memory, on the JVM via
 * Robolectric): the SQL ordering, the search query, the 50-item cap and
 * pruning — behavior the fake-DAO tests cannot prove, because there the
 * "database" is a Kotlin list that always agrees with the code under test.
 *
 * The sdk pin selects the Robolectric runtime only (JDK 17 supports up
 * to API 35; see docs/VERSIONS.md, Phase 7 addendum). The app itself
 * still compiles and targets API 37.1.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RecentDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RecentFilesRepository
    private var clock = 0L

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RecentFilesRepository(
            recentDao = database.recentFileDao(),
            folderDao = database.folderRootDao(),
            nowMillis = { clock }
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun recentsNow(): List<RecentFile> =
        withTimeout(10_000) { repository.recents.first() }

    private suspend fun record(uri: String, name: String, at: Long) {
        clock = at
        repository.recordOpened(uri = uri, name = name, folderLabel = "Docs")
    }

    @Test
    fun recentsComeBackNewestFirst() = runBlocking {
        record("content://a", "a.md", at = 1)
        record("content://b", "b.md", at = 2)
        record("content://c", "c.md", at = 3)

        assertEquals(
            listOf("c.md", "b.md", "a.md"),
            recentsNow().map { it.name }
        )
    }

    @Test
    fun reopeningAFileMovesItToTheFrontWithoutDuplicating() = runBlocking {
        record("content://a", "a.md", at = 1)
        record("content://b", "b.md", at = 2)
        record("content://a", "a.md", at = 3)

        val recents = recentsNow()
        assertEquals(2, recents.size)
        assertEquals(listOf("a.md", "b.md"), recents.map { it.name })
        assertEquals(3L, recents.first().lastOpenedAt)
    }

    @Test
    fun theCapKeepsTheNewestFifty() = runBlocking {
        for (i in 1..55) {
            record("content://f$i", "file$i.md", at = i.toLong())
        }

        val recents = recentsNow()
        assertEquals(RecentFilesRepository.MAX_RECENTS, recents.size)
        assertEquals("file55.md", recents.first().name)
        val uris = recents.map { it.uri }.toSet()
        assertTrue("content://f6" in uris)
        assertFalse("content://f5" in uris)
        assertFalse("content://f1" in uris)
    }

    @Test
    fun searchMatchesByNameSubstring() = runBlocking {
        record("content://1", "Shopping list.md", at = 1)
        record("content://2", "README.md", at = 2)

        val hits = withTimeout(10_000) {
            repository.searchRecents("shop").first()
        }
        assertEquals(listOf("Shopping list.md"), hits.map { it.name })

        val all = withTimeout(10_000) {
            repository.searchRecents("  ").first()
        }
        assertEquals(2, all.size)
    }

    @Test
    fun pruneRemovesOnlyInaccessibleRecents() = runBlocking {
        record("content://a", "a.md", at = 1)
        record("content://b", "b.md", at = 2)
        record("content://c", "c.md", at = 3)

        repository.pruneInaccessible { uri -> uri != "content://b" }

        assertEquals(
            listOf("c.md", "a.md"),
            recentsNow().map { it.name }
        )
    }

    @Test
    fun removeRecentDeletesByUri() = runBlocking {
        record("content://a", "a.md", at = 1)
        record("content://b", "b.md", at = 2)

        repository.removeRecent("content://a")

        assertEquals(listOf("b.md"), recentsNow().map { it.name })
    }

    @Test
    fun folderRootsAreObservedInAddOrderAndRemovable() = runBlocking {
        clock = 1
        repository.addFolder("content://tree/1", "Docs")
        clock = 2
        repository.addFolder("content://tree/2", "Notes")

        val roots = withTimeout(10_000) { repository.folderRoots.first() }
        assertEquals(listOf("Docs", "Notes"), roots.map { it.name })

        repository.removeFolder("content://tree/1")

        val after = withTimeout(10_000) { repository.folderRoots.first() }
        assertEquals(listOf("Notes"), after.map { it.name })
    }
}
