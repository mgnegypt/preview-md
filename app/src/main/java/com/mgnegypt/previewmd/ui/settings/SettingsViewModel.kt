package com.mgnegypt.previewmd.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mgnegypt.previewmd.data.settings.ReleaseChannel
import com.mgnegypt.previewmd.data.settings.SettingsRepository
import com.mgnegypt.previewmd.data.settings.SettingsState
import com.mgnegypt.previewmd.data.settings.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsState> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsState()
    )

    fun onThemeModeChange(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    /**
     * Persists the choice and applies it through AppCompat. Applying a new
     * locale recreates the activity, which is the platform behavior for
     * per-app languages; the stored value keeps the choice across restarts.
     */
    fun onLanguageChange(tag: String) {
        viewModelScope.launch { repository.setLanguageTag(tag) }
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(tag)
        )
    }

    fun onAutoUpdateChange(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoUpdate(enabled) }
    }

    fun onChannelChange(channel: ReleaseChannel) {
        viewModelScope.launch { repository.setChannel(channel) }
    }

    class Factory(
        private val repository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return SettingsViewModel(repository) as T
        }
    }
}
