package com.mgnegypt.previewmd.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Verifies the defaults a fresh install must see and that every setter
 * round-trips through real DataStore storage (temporary file on the JVM).
 */
class SettingsRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var scope: CoroutineScope
    private lateinit var repository: SettingsRepository

    @Before
    fun setUp() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = File(tempFolder.newFolder(), "settings.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }
        repository = SettingsRepository(dataStore)
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun defaults_matchTheDesign() = runTest {
        val state = repository.settings.first()
        assertEquals(ThemeMode.SYSTEM, state.themeMode)
        assertNull(state.languageTag)
        assertTrue(state.autoUpdate)
        assertEquals(ReleaseChannel.STABLE, state.channel)
        assertFalse(state.onboardingDone)
    }

    @Test
    fun setters_roundTrip() = runTest {
        repository.setThemeMode(ThemeMode.DARK)
        repository.setLanguageTag("ar")
        repository.setAutoUpdate(false)
        repository.setChannel(ReleaseChannel.BETA)
        repository.setOnboardingDone(true)

        val state = repository.settings.first()
        assertEquals(ThemeMode.DARK, state.themeMode)
        assertEquals("ar", state.languageTag)
        assertFalse(state.autoUpdate)
        assertEquals(ReleaseChannel.BETA, state.channel)
        assertTrue(state.onboardingDone)
    }

    @Test
    fun languageTag_canBeClearedBackToSystem() = runTest {
        repository.setLanguageTag("ar")
        repository.setLanguageTag(null)
        assertNull(repository.settings.first().languageTag)
    }
}
