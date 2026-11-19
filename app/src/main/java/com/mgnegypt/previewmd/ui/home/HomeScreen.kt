package com.mgnegypt.previewmd.ui.home

import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mgnegypt.previewmd.R
import com.mgnegypt.previewmd.data.settings.ThemeMode
import com.mgnegypt.previewmd.ui.components.FileCard
import com.mgnegypt.previewmd.ui.components.PillButtonSecondary
import com.mgnegypt.previewmd.ui.components.RoundIconButton
import com.mgnegypt.previewmd.ui.components.SegmentedPill
import com.mgnegypt.previewmd.ui.theme.FileCardColors
import com.mgnegypt.previewmd.ui.theme.PillShape
import com.mgnegypt.previewmd.util.RelativeBucket
import com.mgnegypt.previewmd.util.relativeTime

/**
 * Home, as drawn: app label and title, the theme toggle, a search pill,
 * the Recent|Folders segmented control and the colored file cards.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenFile: (Uri) -> Unit,
    onOpenFileRequest: () -> Unit,
    onAddFolderRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val effectiveDark = when (state.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            RoundIconButton(
                icon = if (effectiveDark) {
                    Icons.Filled.LightMode
                } else {
                    Icons.Filled.DarkMode
                },
                contentDescription = stringResource(R.string.theme_toggle),
                onClick = { viewModel.onToggleTheme(effectiveDark) }
            )
        }

        SearchPill(
            query = state.query,
            onQueryChange = viewModel::onQueryChange
        )

        SegmentedPill(
            options = listOf(
                stringResource(R.string.home_recent),
                stringResource(R.string.home_folders)
            ),
            selectedIndex = if (state.tab == HomeTab.FOLDERS) 1 else 0,
            onSelected = { index ->
                viewModel.onTabChange(
                    if (index == 1) HomeTab.FOLDERS else HomeTab.RECENT
                )
            }
        )

        when (state.tab) {
            HomeTab.RECENT -> RecentList(
                state = state,
                onOpenFile = onOpenFile,
                onOpenFileRequest = onOpenFileRequest,
                viewModel = viewModel
            )
            HomeTab.FOLDERS -> FolderList(
                state = state,
                onOpenFile = onOpenFile,
                onAddFolderRequest = onAddFolderRequest,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun RecentList(
    state: HomeUiState,
    viewModel: HomeViewModel,
    onOpenFile: (Uri) -> Unit,
    onOpenFileRequest: () -> Unit
) {
    if (state.recents.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.home_empty_recents),
            body = stringResource(R.string.home_empty_recents_body),
            actionLabel = stringResource(R.string.welcome_open_file),
            onAction = onOpenFileRequest
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(state.recents, key = { _, item -> item.uri }) { index, recent ->
            FileCard(
                name = recent.name,
                path = recent.folderLabel,
                timeLabel = relativeLabel(recent.lastOpenedAt),
                color = FileCardColors[index % FileCardColors.size],
                onClick = {
                    viewModel.onRecentOpened(recent)
                    onOpenFile(Uri.parse(recent.uri))
                }
            )
        }
    }
}

@Composable
private fun FolderList(
    state: HomeUiState,
    viewModel: HomeViewModel,
    onOpenFile: (Uri) -> Unit,
    onAddFolderRequest: () -> Unit
) {
    if (state.folders.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.home_empty_folders),
            body = stringResource(R.string.home_empty_folders_body),
            actionLabel = stringResource(R.string.home_add_folder),
            onAction = onAddFolderRequest
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        state.folders.forEach { section ->
            item(key = "root-${section.root.uri}") {
                Text(
                    text = section.root.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            itemsIndexed(
                section.files,
                key = { _, child -> child.uri.toString() }
            ) { index, child ->
                FileCard(
                    name = child.name,
                    path = section.root.name,
                    timeLabel = relativeLabel(child.lastModified),
                    color = FileCardColors[index % FileCardColors.size],
                    onClick = {
                        viewModel.onFolderFileOpened(child, section.root)
                        onOpenFile(child.uri)
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )
        PillButtonSecondary(text = actionLabel, onClick = onAction)
    }
}

@Composable
private fun SearchPill(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = PillShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val textState = rememberTextFieldState(initialText = query)
            LaunchedEffect(textState) {
                snapshotFlow { textState.text.toString() }
                    .collect { onQueryChange(it) }
            }
            LaunchedEffect(query) {
                if (textState.text.toString() != query) {
                    textState.setTextAndPlaceCursorAtEnd(query)
                }
            }
            BasicTextField(
                state = textState,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                lineLimits = TextFieldLineLimits.SingleLine,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp),
                decorator = { innerTextField ->
                    Box {
                        if (textState.text.isEmpty()) {
                            Text(
                                text = stringResource(
                                    R.string.home_search_placeholder
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
private fun relativeLabel(lastOpenedAt: Long): String {
    val now = remember { System.currentTimeMillis() }
    val relative = relativeTime(now, lastOpenedAt)
    return when (relative.bucket) {
        RelativeBucket.NOW -> stringResource(R.string.time_now)
        RelativeBucket.MINUTES -> pluralStringResource(
            R.plurals.time_minutes,
            relative.count,
            relative.count
        )
        RelativeBucket.HOURS -> pluralStringResource(
            R.plurals.time_hours,
            relative.count,
            relative.count
        )
        RelativeBucket.YESTERDAY -> stringResource(R.string.time_yesterday)
        RelativeBucket.DAYS -> pluralStringResource(
            R.plurals.time_days,
            relative.count,
            relative.count
        )
    }
}
