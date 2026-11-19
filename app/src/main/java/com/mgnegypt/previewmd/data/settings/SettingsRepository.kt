package com.mgnegypt.previewmd.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class ReleaseChannel {
    STABLE,
    BETA
}

/**
 * The user's settings. [languageTag] is null when the user has not chosen a
 * language in the app yet; the app then follows the system language.
 */
data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageTag: String? = null,
    val autoUpdate: Boolean = true,
    val channel: ReleaseChannel = ReleaseChannel.STABLE,
    val onboardingDone: Boolean = false
)

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings"
)

/**
 * Single source of truth for app settings, backed by DataStore Preferences.
 * All reads are exposed as one [Flow] of [SettingsState]; writes go through
 * the typed setters so stored values can never drift from the enums above.
 * The primary constructor takes the [DataStore] so the repository can be
 * unit-tested on the JVM with a temporary file.
 */
class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    constructor(context: Context) : this(
        context.applicationContext.settingsDataStore
    )

    val settings: Flow<SettingsState> = dataStore.data.map { prefs ->
        SettingsState(
            themeMode = prefs[KEY_THEME]?.let { stored ->
                ThemeMode.entries.firstOrNull { it.name == stored }
            } ?: ThemeMode.SYSTEM,
            languageTag = prefs[KEY_LANGUAGE],
            autoUpdate = prefs[KEY_AUTO_UPDATE] ?: true,
            channel = prefs[KEY_CHANNEL]?.let { stored ->
                ReleaseChannel.entries.firstOrNull { it.name == stored }
            } ?: ReleaseChannel.STABLE,
            onboardingDone = prefs[KEY_ONBOARDING_DONE] ?: false
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME] = mode.name }
    }

    suspend fun setLanguageTag(tag: String?) {
        dataStore.edit { prefs ->
            if (tag == null) prefs.remove(KEY_LANGUAGE) else prefs[KEY_LANGUAGE] = tag
        }
    }

    suspend fun setAutoUpdate(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_UPDATE] = enabled }
    }

    suspend fun setChannel(channel: ReleaseChannel) {
        dataStore.edit { it[KEY_CHANNEL] = channel.name }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING_DONE] = done }
    }

    private companion object {
        val KEY_THEME = stringPreferencesKey("theme_mode")
        val KEY_LANGUAGE = stringPreferencesKey("language_tag")
        val KEY_AUTO_UPDATE = booleanPreferencesKey("auto_update")
        val KEY_CHANNEL = stringPreferencesKey("release_channel")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }
}
