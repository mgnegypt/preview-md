package com.mgnegypt.previewmd.ui.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mgnegypt.previewmd.R
import com.mgnegypt.previewmd.ui.components.DesignToggle
import com.mgnegypt.previewmd.ui.components.FileCard
import com.mgnegypt.previewmd.ui.components.FloatingBottomBar
import com.mgnegypt.previewmd.ui.components.PillButtonAccent
import com.mgnegypt.previewmd.ui.components.PillButtonPrimary
import com.mgnegypt.previewmd.ui.components.PillButtonSecondary
import com.mgnegypt.previewmd.ui.components.ProgressPill
import com.mgnegypt.previewmd.ui.components.RoundIconButton
import com.mgnegypt.previewmd.ui.components.SegmentedPill
import com.mgnegypt.previewmd.ui.components.SettingsCard
import com.mgnegypt.previewmd.ui.theme.AccentPink
import com.mgnegypt.previewmd.ui.theme.AccentYellow
import com.mgnegypt.previewmd.ui.theme.Cream
import com.mgnegypt.previewmd.ui.theme.FileCardColors
import com.mgnegypt.previewmd.ui.theme.PreviewMDTheme

/**
 * Phase 1 design-system gallery: every component rendered in the light and
 * the dark theme on one scrollable screen, so the design can be checked on
 * a real device from the debug build. Phase 2 replaces this with the app
 * shell; the components stay.
 */
@Composable
fun DesignGalleryScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        GallerySection(darkTheme = false)
        GallerySection(darkTheme = true)
    }
}

@Composable
private fun GallerySection(darkTheme: Boolean) {
    PreviewMDTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(
                        if (darkTheme) R.string.gallery_dark else R.string.gallery_light
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoundIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        onClick = {}
                    )
                    Text(
                        text = stringResource(R.string.design_gallery_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    RoundIconButton(
                        icon = Icons.Filled.Edit,
                        contentDescription = null,
                        onClick = {}
                    )
                }
                PillButtonPrimary(
                    text = stringResource(R.string.gallery_primary_action),
                    onClick = {},
                    trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight
                )
                PillButtonSecondary(
                    text = stringResource(R.string.gallery_secondary_action),
                    onClick = {}
                )
                var segment by remember { mutableIntStateOf(0) }
                SegmentedPill(
                    options = listOf(
                        stringResource(R.string.gallery_segment_recent),
                        stringResource(R.string.gallery_segment_folders)
                    ),
                    selectedIndex = segment,
                    onSelected = { segment = it }
                )
                FileCardColors.forEachIndexed { index, color ->
                    FileCard(
                        name = "README.md",
                        path = "github/preview-md",
                        timeLabel = "2 min",
                        color = color,
                        onClick = {}
                    )
                }
                var updatesOn by remember { mutableStateOf(true) }
                SettingsCard(color = AccentYellow) {
                    Text(
                        text = stringResource(R.string.gallery_updates),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.gallery_auto_updates),
                            style = MaterialTheme.typography.titleMedium
                        )
                        DesignToggle(
                            checked = updatesOn,
                            onCheckedChange = { updatesOn = it }
                        )
                    }
                    SegmentedPill(
                        options = listOf(
                            stringResource(R.string.gallery_channel_stable),
                            stringResource(R.string.gallery_channel_beta)
                        ),
                        selectedIndex = 0,
                        onSelected = {},
                        containerColor = Cream
                    )
                }
                SettingsCard(color = AccentPink) {
                    Text(
                        text = stringResource(R.string.gallery_update_available),
                        style = MaterialTheme.typography.titleMedium
                    )
                    ProgressPill(progress = 0.4f)
                    PillButtonPrimary(
                        text = stringResource(R.string.gallery_download),
                        onClick = {},
                        leadingIcon = Icons.Filled.Download
                    )
                }
                PillButtonAccent(
                    text = stringResource(R.string.gallery_accent_action),
                    onClick = {}
                )
                FloatingBottomBar(
                    selectedIndex = 0,
                    onItemSelected = {},
                    onCenterClick = {}
                )
            }
        }
    }
}
