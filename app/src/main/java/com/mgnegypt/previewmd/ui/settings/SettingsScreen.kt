package com.mgnegypt.previewmd.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mgnegypt.previewmd.R
import com.mgnegypt.previewmd.data.settings.ReleaseChannel
import com.mgnegypt.previewmd.data.settings.ThemeMode
import com.mgnegypt.previewmd.ui.components.DesignToggle
import com.mgnegypt.previewmd.ui.components.RoundIconButton
import com.mgnegypt.previewmd.ui.components.SegmentedPill
import com.mgnegypt.previewmd.ui.components.SettingsCard
import com.mgnegypt.previewmd.ui.theme.AccentYellow
import com.mgnegypt.previewmd.ui.theme.Cream

/**
 * Settings, as drawn: Appearance and Language segmented controls, then the
 * yellow Updates card (version, automatic updates, release channel). The
 * update-available card and the check button arrive with the updater in
 * Phase 6, where they become functional.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val versionName = rememberVersionName()
    val currentLanguage = state.languageTag
        ?: LocalConfiguration.current.locales[0].language

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            RoundIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Text(
            text = stringResource(R.string.settings_appearance),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )
        SegmentedPill(
            options = listOf(
                stringResource(R.string.theme_light),
                stringResource(R.string.theme_dark),
                stringResource(R.string.theme_system)
            ),
            selectedIndex = when (state.themeMode) {
                ThemeMode.LIGHT -> 0
                ThemeMode.DARK -> 1
                ThemeMode.SYSTEM -> 2
            },
            onSelected = { index ->
                viewModel.onThemeModeChange(
                    when (index) {
                        0 -> ThemeMode.LIGHT
                        1 -> ThemeMode.DARK
                        else -> ThemeMode.SYSTEM
                    }
                )
            }
        )

        Text(
            text = stringResource(R.string.settings_language),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )
        SegmentedPill(
            options = listOf(
                stringResource(R.string.language_english),
                stringResource(R.string.language_arabic)
            ),
            selectedIndex = if (currentLanguage == "ar") 1 else 0,
            onSelected = { index ->
                viewModel.onLanguageChange(if (index == 1) "ar" else "en")
            }
        )

        SettingsCard(
            color = AccentYellow,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_updates),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.settings_version, versionName),
                style = MaterialTheme.typography.bodyLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_auto_updates),
                    style = MaterialTheme.typography.titleMedium
                )
                DesignToggle(
                    checked = state.autoUpdate,
                    onCheckedChange = viewModel::onAutoUpdateChange
                )
            }
            Text(
                text = stringResource(R.string.settings_channel),
                style = MaterialTheme.typography.titleMedium
            )
            SegmentedPill(
                options = listOf(
                    stringResource(R.string.channel_stable),
                    stringResource(R.string.channel_beta)
                ),
                selectedIndex = if (state.channel == ReleaseChannel.BETA) 1 else 0,
                onSelected = { index ->
                    viewModel.onChannelChange(
                        if (index == 1) ReleaseChannel.BETA else ReleaseChannel.STABLE
                    )
                },
                containerColor = Cream
            )
        }
    }
}

@Composable
private fun rememberVersionName(): String {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: ""
    }
}
