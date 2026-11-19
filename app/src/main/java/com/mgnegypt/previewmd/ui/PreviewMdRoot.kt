package com.mgnegypt.previewmd.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mgnegypt.previewmd.AppContainer
import com.mgnegypt.previewmd.data.settings.ThemeMode
import com.mgnegypt.previewmd.ui.shell.AppShell
import com.mgnegypt.previewmd.ui.theme.PreviewMDTheme
import com.mgnegypt.previewmd.ui.welcome.WelcomeScreen
import kotlinx.coroutines.launch

/**
 * The composition root: applies the stored theme, shows Welcome until
 * onboarding is done, then the navigation shell.
 */
@Composable
fun PreviewMdRoot(container: AppContainer) {
    val settings by container.settingsRepository.settings
        .collectAsStateWithLifecycle(initialValue = null)
    val state = settings
    val scope = rememberCoroutineScope()

    val darkTheme = when (state?.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        else -> isSystemInDarkTheme()
    }

    PreviewMDTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when {
                state == null -> {
                    // Settings are still loading from DataStore; the themed
                    // surface is shown for the few frames this takes.
                }
                !state.onboardingDone -> {
                    WelcomeScreen(
                        onGetStarted = {
                            scope.launch {
                                container.settingsRepository.setOnboardingDone(true)
                            }
                        },
                        onOpenFile = {
                            // Phase 3 inserts the file picker before Home;
                            // the onboarding outcome is the same.
                            scope.launch {
                                container.settingsRepository.setOnboardingDone(true)
                            }
                        }
                    )
                }
                else -> AppShell(container = container)
            }
        }
    }
}
