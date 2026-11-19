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
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
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
import com.mgnegypt.previewmd.data.update.UpdateErrorKind
import com.mgnegypt.previewmd.data.update.UpdateUiState
import com.mgnegypt.previewmd.ui.components.DesignToggle
import com.mgnegypt.previewmd.ui.components.PillButtonPrimary
import com.mgnegypt.previewmd.ui.components.ProgressPill
import com.mgnegypt.previewmd.ui.components.RoundIconButton
import com.mgnegypt.previewmd.ui.components.SegmentedPill
import com.mgnegypt.previewmd.ui.components.SettingsCard
import com.mgnegypt.previewmd.ui.theme.AccentPink
import com.mgnegypt.previewmd.ui.theme.AccentYellow
import com.mgnegypt.previewmd.ui.theme.Ink
import com.mgnegypt.previewmd.ui.theme.Cream

/**
 * Settings, as drawn: Appearance and Language segmented controls, then the
 * yellow Updates card (version, automatic updates, release channel, the
 * check button and its status), and below it the pink update-available
 * card whenever the updater has something to offer.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
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
            UpdateCheckSection(
                state = updateState,
                onCheck = viewModel::onCheckUpdates
            )
        }

        UpdateOfferCard(
            state = updateState,
            viewModel = viewModel
        )
    }
}

/** The check button and its one-line status, inside the yellow card. */
@Composable
private fun UpdateCheckSection(
    state: UpdateUiState,
    onCheck: () -> Unit
) {
    when (state) {
        UpdateUiState.Checking -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    color = Ink,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = stringResource(R.string.update_checking),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        is UpdateUiState.Error -> {
            Text(
                text = stringResource(updateErrorText(state.kind)),
                style = MaterialTheme.typography.bodyLarge
            )
            PillButtonPrimary(
                text = stringResource(R.string.update_check),
                onClick = onCheck
            )
        }

        UpdateUiState.UpToDate -> {
            Text(
                text = stringResource(R.string.update_up_to_date),
                style = MaterialTheme.typography.bodyLarge
            )
            PillButtonPrimary(
                text = stringResource(R.string.update_check),
                onClick = onCheck
            )
        }

        UpdateUiState.Installed -> {
            Text(
                text = stringResource(R.string.update_installed),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        else -> {
            PillButtonPrimary(
                text = stringResource(R.string.update_check),
                onClick = onCheck
            )
        }
    }
}

/**
 * The pink card under the yellow one, present while an update is
 * available, downloading, verifying or ready to install.
 */
@Composable
private fun UpdateOfferCard(
    state: UpdateUiState,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val info = when (state) {
        is UpdateUiState.Available -> state.info
        is UpdateUiState.Downloading -> state.info
        is UpdateUiState.Verifying -> state.info
        is UpdateUiState.ReadyToInstall -> state.info
        else -> null
    } ?: return

    SettingsCard(
        color = AccentPink,
        modifier = Modifier.padding(top = 2.dp)
    ) {
        Text(
            text = stringResource(R.string.update_available_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(
                R.string.update_available_version,
                info.version.toString()
            ),
            style = MaterialTheme.typography.bodyLarge
        )
        when (state) {
            is UpdateUiState.Available -> {
                PillButtonPrimary(
                    text = stringResource(R.string.update_download),
                    onClick = { viewModel.onDownloadUpdate(state.info) }
                )
            }

            is UpdateUiState.Downloading -> {
                ProgressPill(progress = state.fraction)
                Text(
                    text = stringResource(
                        R.string.update_downloading,
                        (state.fraction * 100).toInt()
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            is UpdateUiState.Verifying -> {
                Text(
                    text = stringResource(R.string.update_verifying),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            is UpdateUiState.ReadyToInstall -> {
                PillButtonPrimary(
                    text = stringResource(R.string.update_install),
                    onClick = {
                        if (viewModel.canInstall(context)) {
                            viewModel.onInstallUpdate(state.file)
                        } else {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    }
                )
                if (!viewModel.canInstall(context)) {
                    Text(
                        text = stringResource(R.string.update_allow_installs),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            else -> Unit
        }
    }

    if (state is UpdateUiState.Installing) {
        Text(
            text = stringResource(R.string.update_installing),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

private fun updateErrorText(kind: UpdateErrorKind): Int = when (kind) {
    UpdateErrorKind.OFFLINE -> R.string.update_error_offline
    UpdateErrorKind.RATE_LIMITED -> R.string.update_error_rate_limited
    UpdateErrorKind.NO_RELEASE -> R.string.update_error_no_release
    UpdateErrorKind.SERVER, UpdateErrorKind.PARSE ->
        R.string.update_error_server

    UpdateErrorKind.DOWNLOAD_FAILED -> R.string.update_error_download
    UpdateErrorKind.CHECKSUM_MISMATCH, UpdateErrorKind.NO_CHECKSUM ->
        R.string.update_error_checksum

    UpdateErrorKind.INSTALL_FAILED -> R.string.update_error_install
    UpdateErrorKind.NO_APK_ASSET -> R.string.update_error_no_apk
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
