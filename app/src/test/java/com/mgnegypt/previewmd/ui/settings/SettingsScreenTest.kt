package com.mgnegypt.previewmd.ui.settings

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.mgnegypt.previewmd.data.files.FileRepository
import com.mgnegypt.previewmd.data.settings.SettingsRepository
import com.mgnegypt.previewmd.data.update.UpdateInstaller
import com.mgnegypt.previewmd.data.update.UpdateManager
import com.mgnegypt.previewmd.data.update.UpdateRepository
import com.mgnegypt.previewmd.ui.theme.PreviewMDTheme
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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
 * The real Settings screen, composed on the JVM (Robolectric): the
 * Appearance and Language controls, the yellow Updates card with its
 * version line, and — the state a fresh screen starts in — the Check
 * for updates button. The tall qualifiers give the scrollable column
 * room so every node is actually on screen.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h2200dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var dataStoreScope: CoroutineScope

    @Before
    fun setUp() {
        dataStoreScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = File(tempFolder.newFolder(), "settings.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope
        ) { file }
        val settingsRepository = SettingsRepository(dataStore)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val updateManager = UpdateManager(
            context = context,
            repository = UpdateRepository(context),
            installer = UpdateInstaller(),
            settings = settingsRepository
        )
        val viewModel = SettingsViewModel(
            settingsRepository,
            updateManager,
            FileRepository(context)
        )
        composeRule.setContent {
            PreviewMDTheme(darkTheme = false) {
                SettingsScreen(viewModel = viewModel, onBack = {})
            }
        }
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun appearanceAndLanguageControlsAreShown() {
        composeRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeRule.onNodeWithText("Light").assertIsDisplayed()
        composeRule.onNodeWithText("Dark").assertIsDisplayed()
        composeRule.onNodeWithText("Language").assertIsDisplayed()
        composeRule.onNodeWithText("English").assertIsDisplayed()
    }

    @Test
    fun updatesCardShowsVersionAndCheckButton() {
        composeRule.onNodeWithText("Updates").assertIsDisplayed()
        composeRule.onNodeWithText("Version", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("Automatic updates").assertIsDisplayed()
        composeRule.onNodeWithText("Check for updates").assertIsDisplayed()
    }

    @Test
    fun appFolderCardShowsSetupWhenUnconfigured() {
        composeRule.onNodeWithText("App folder").assertIsDisplayed()
        composeRule.onNodeWithText("Not set up yet").assertIsDisplayed()
        composeRule.onNodeWithText("Set up").assertIsDisplayed()
    }

    @Test
    fun channelOptionsAreShownAndBetaCanBeSelected() {
        composeRule.onNodeWithText("Release channel").assertIsDisplayed()
        composeRule.onNodeWithText("Stable").assertIsDisplayed()
        composeRule.onNodeWithText("Beta").assertIsDisplayed()

        composeRule.onNodeWithText("Beta").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Beta").assertIsDisplayed()
        composeRule.onNodeWithText("Check for updates").assertIsDisplayed()
    }
}
