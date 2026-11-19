package com.mgnegypt.previewmd.ui.home

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mgnegypt.previewmd.data.files.FileRepository
import com.mgnegypt.previewmd.data.recent.AppDatabase
import com.mgnegypt.previewmd.data.recent.RecentFilesRepository
import com.mgnegypt.previewmd.data.settings.SettingsRepository
import com.mgnegypt.previewmd.ui.theme.PreviewMDTheme
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The real Home screen, composed on the JVM (Robolectric) over a real
 * in-memory Room database: the title, the Recent|Folders segmented
 * control, the empty state, and a recorded file appearing as a card.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h1400dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var database: AppDatabase
    private lateinit var recentFiles: RecentFilesRepository
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        recentFiles = RecentFilesRepository(
            recentDao = database.recentFileDao(),
            folderDao = database.folderRootDao()
        )
        dataStoreScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = File(tempFolder.newFolder(), "settings.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope
        ) { file }
        viewModel = HomeViewModel(
            recentFiles = recentFiles,
            files = FileRepository(context),
            settings = SettingsRepository(dataStore)
        )
    }

    @After
    fun tearDown() {
        database.close()
        dataStoreScope.cancel()
    }

    private fun composeHome() {
        composeRule.setContent {
            PreviewMDTheme(darkTheme = false) {
                HomeScreen(
                    viewModel = viewModel,
                    onOpenFile = {},
                    onOpenFileRequest = {},
                    onAddFolderRequest = {}
                )
            }
        }
    }

    @Test
    fun titleSegmentsAndEmptyStateAreShown() {
        composeHome()

        composeRule.onNodeWithText("Your files").assertIsDisplayed()
        composeRule.onNodeWithText("Recent").assertIsDisplayed()
        composeRule.onNodeWithText("Folders").assertIsDisplayed()
        composeRule.onNodeWithText("No files yet").assertIsDisplayed()
    }

    @Test
    fun aRecordedFileAppearsAsACard() {
        runBlocking {
            recentFiles.recordOpened(
                uri = "content://docs/notes",
                name = "notes.md",
                folderLabel = "Docs"
            )
        }

        composeHome()

        composeRule.waitUntilExactlyOneExists(
            hasText("notes.md"),
            timeoutMillis = 10_000
        )
        composeRule.onNodeWithText("notes.md").assertIsDisplayed()
    }
}
