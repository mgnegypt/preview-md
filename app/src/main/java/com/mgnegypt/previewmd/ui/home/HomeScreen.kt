package com.mgnegypt.previewmd.ui.home

import android.net.Uri
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mgnegypt.previewmd.R
import com.mgnegypt.previewmd.data.files.TreeChild
import com.mgnegypt.previewmd.data.recent.FolderRoot
import com.mgnegypt.previewmd.data.recent.RecentFile
import com.mgnegypt.previewmd.data.settings.ThemeMode
import com.mgnegypt.previewmd.ui.components.FileCard
import com.mgnegypt.previewmd.ui.components.PillButtonSecondary
import com.mgnegypt.previewmd.ui.components.RoundIconButton
import com.mgnegypt.previewmd.ui.components.SegmentedPill
import com.mgnegypt.previewmd.ui.theme.AccentBlue
import com.mgnegypt.previewmd.ui.theme.FileCardColors
import com.mgnegypt.previewmd.ui.theme.Ink
import com.mgnegypt.previewmd.ui.theme.PillShape
import com.mgnegypt.previewmd.util.RelativeBucket
import com.mgnegypt.previewmd.util.relativeTime
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

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

    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val messageText = when (message) {
        HomeMessage.Deleted -> stringResource(R.string.home_msg_deleted)
        HomeMessage.DeleteFailed -> stringResource(
            R.string.home_msg_delete_failed
        )
        HomeMessage.Copied -> stringResource(R.string.home_msg_copied)
        HomeMessage.CopiedToAppFolder -> stringResource(
            R.string.home_msg_copied_app_folder
        )
        HomeMessage.CopyFailed -> stringResource(
            R.string.home_msg_copy_failed
        )
        HomeMessage.CopyNeedsAppFolder -> stringResource(
            R.string.home_msg_copy_needs_folder
        )
        null -> null
    }
    LaunchedEffect(message) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            viewModel.onMessageShown()
        }
    }

    var deleteTarget by remember { mutableStateOf<DeleteTarget?>(null) }

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

        Box(modifier = Modifier.weight(1f)) {
            when (state.tab) {
                HomeTab.RECENT -> RecentList(
                    state = state,
                    onOpenFile = onOpenFile,
                    onOpenFileRequest = onOpenFileRequest,
                    viewModel = viewModel,
                    onDelete = { recent ->
                        deleteTarget = DeleteTarget(recent.name) {
                            viewModel.onDeleteRecent(recent)
                        }
                    },
                    onCopy = { recent -> viewModel.onCopyRecent(recent) }
                )
                HomeTab.FOLDERS -> FolderList(
                    state = state,
                    onOpenFile = onOpenFile,
                    onAddFolderRequest = onAddFolderRequest,
                    viewModel = viewModel,
                    onDelete = { child ->
                        deleteTarget = DeleteTarget(child.name) {
                            viewModel.onDeleteFolderFile(child)
                        }
                    },
                    onCopy = { child, root ->
                        viewModel.onCopyFolderFile(child, root)
                    }
                )
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }
    }

    val target = deleteTarget
    if (target != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(text = stringResource(R.string.home_delete_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.home_delete_message,
                        target.name
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteTarget = null
                        target.action()
                    }
                ) {
                    Text(text = stringResource(R.string.home_action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(text = stringResource(R.string.editor_cancel))
                }
            }
        )
    }
}

/** A pending delete confirmation: the file name and what confirms it. */
private data class DeleteTarget(
    val name: String,
    val action: () -> Unit
)

@Composable
private fun RecentList(
    state: HomeUiState,
    viewModel: HomeViewModel,
    onOpenFile: (Uri) -> Unit,
    onOpenFileRequest: () -> Unit,
    onDelete: (RecentFile) -> Unit,
    onCopy: (RecentFile) -> Unit
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
            SwipeableActions(
                onDelete = { onDelete(recent) },
                onCopy = { onCopy(recent) }
            ) {
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
}

@Composable
private fun FolderList(
    state: HomeUiState,
    viewModel: HomeViewModel,
    onOpenFile: (Uri) -> Unit,
    onAddFolderRequest: () -> Unit,
    onDelete: (TreeChild) -> Unit,
    onCopy: (TreeChild, FolderRoot) -> Unit
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
                SwipeableActions(
                    onDelete = { onDelete(child) },
                    onCopy = { onCopy(child, section.root) }
                ) {
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

private enum class SwipeAnchor { DeleteOpen, Closed, CopyOpen }

private val SwipeActionWidth = 84.dp

/**
 * Swipe actions around a file card, as requested: drag right and a
 * Delete action rests open at the start side; drag left and a Copy
 * action rests open at the end side. Tapping an action runs it and
 * closes the card; tapping the open card just closes it. The card
 * itself never leaves the list from the gesture alone.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun SwipeableActions(
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val actionWidthPx = with(density) { SwipeActionWidth.toPx() }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val state = remember(actionWidthPx) {
        AnchoredDraggableState(
            initialValue = SwipeAnchor.Closed,
            anchors = DraggableAnchors {
                SwipeAnchor.DeleteOpen at actionWidthPx
                SwipeAnchor.Closed at 0f
                SwipeAnchor.CopyOpen at -actionWidthPx
            },
            positionalThreshold = { distance -> distance * 0.5f },
            velocityThreshold = { with(density) { 125.dp.toPx() } },
            snapAnimationSpec = spring(),
            decayAnimationSpec = androidx.compose.animation.core
                .exponentialDecay()
        )
    }
    val scope = rememberCoroutineScope()
    fun close() {
        scope.launch { state.animateTo(SwipeAnchor.Closed) }
    }
    Box {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(28.dp))
        ) {
            SwipeStrip(
                color = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                icon = Icons.Filled.Delete,
                label = stringResource(R.string.home_action_delete),
                onClick = {
                    close()
                    onDelete()
                },
                modifier = Modifier.align(Alignment.CenterStart)
            )
            SwipeStrip(
                color = AccentBlue,
                contentColor = Ink,
                icon = Icons.Filled.ContentCopy,
                label = stringResource(R.string.home_action_copy),
                onClick = {
                    close()
                    onCopy()
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(state.requireOffset().roundToInt(), 0)
                }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal,
                    reverseDirection = isRtl
                )
        ) {
            content()
            if (state.currentValue != SwipeAnchor.Closed) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { close() }
                )
            }
        }
    }
}

@Composable
private fun SwipeStrip(
    color: Color,
    contentColor: Color,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(SwipeActionWidth)
            .fillMaxHeight()
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
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
