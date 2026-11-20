package com.mgnegypt.previewmd.ui.editor

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mgnegypt.previewmd.R
import com.mgnegypt.previewmd.data.files.FileErrorKind
import com.mgnegypt.previewmd.ui.components.PillButtonAccent
import com.mgnegypt.previewmd.ui.components.PillButtonPrimary
import com.mgnegypt.previewmd.ui.components.RoundIconButton
import com.mgnegypt.previewmd.ui.theme.Cream
import com.mgnegypt.previewmd.ui.theme.DeepBlack
import com.mgnegypt.previewmd.ui.theme.MonoFamily

/**
 * The Markdown editor (design screen 5): beige rounded surface, line
 * numbers that follow wrapped lines, live syntax highlighting (below
 * [HIGHLIGHT_LIMIT] characters), the floating dark toolbar, and the
 * pink OK pill that saves and returns to the preview. Leaving with
 * unsaved changes asks first; a file changed by another app while
 * editing raises a warning instead of being silently overwritten.
 */
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    initialUri: String,
    onBack: () -> Unit,
    onRetargeted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    var showUnsavedDialog by remember { mutableStateOf(false) }

    // Saving a file from outside the app folder needs the one-time app
    // folder setup; the picker result resumes the interrupted save
    // through the stored continuation.
    var onSetupOutcome by remember {
        mutableStateOf<((EditorSaveOutcome) -> Unit)?>(null)
    }
    val setupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            viewModel.onAppFolderPicked(treeUri) { outcome ->
                onSetupOutcome?.invoke(outcome)
            }
        }
    }

    fun handleSaveOutcome(outcome: EditorSaveOutcome) {
        when (outcome) {
            EditorSaveOutcome.SAVED -> {
                val now = viewModel.currentUriString()
                if (now != initialUri) onRetargeted(now) else onBack()
            }

            EditorSaveOutcome.NEEDS_FOLDER_SETUP -> {
                onSetupOutcome = { resumed -> handleSaveOutcome(resumed) }
                setupLauncher.launch(null)
            }

            EditorSaveOutcome.FAILED -> Unit
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.checkExternalChange()
    }

    fun requestBack() {
        if (ui.dirty) showUnsavedDialog = true else onBack()
    }
    BackHandler { requestBack() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            RoundIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                onClick = { requestBack() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = ui.fileName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            RoundIconButton(
                icon = Icons.AutoMirrored.Filled.Undo,
                contentDescription = stringResource(R.string.editor_undo),
                onClick = { viewModel.textState.undoState.undo() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            RoundIconButton(
                icon = Icons.AutoMirrored.Filled.Redo,
                contentDescription = stringResource(R.string.editor_redo),
                onClick = { viewModel.textState.undoState.redo() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            PillButtonAccent(
                text = stringResource(R.string.editor_ok),
                onClick = {
                    if (ui.dirty) {
                        viewModel.save { outcome -> handleSaveOutcome(outcome) }
                    } else {
                        onBack()
                    }
                }
            )
        }

        if (!ui.highlightEnabled && !ui.loading && ui.loadError == null) {
            Text(
                text = stringResource(R.string.editor_highlight_off),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        when {
            ui.loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            ui.loadError != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Spacer(modifier = Modifier.height(48.dp))
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(
                            when (ui.loadError) {
                                FileErrorKind.PERMISSION_LOST ->
                                    R.string.viewer_error_permission

                                FileErrorKind.NOT_FOUND ->
                                    R.string.viewer_error_not_found

                                FileErrorKind.IO_FAILURE ->
                                    R.string.viewer_error_generic

                                null -> R.string.viewer_error_generic
                            }
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PillButtonPrimary(
                        text = stringResource(R.string.viewer_retry),
                        onClick = { viewModel.retryLoad() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            else -> {
                Box(modifier = Modifier.weight(1f)) {
                    EditorBody(
                        state = viewModel.textState,
                        highlightEnabled = ui.highlightEnabled,
                        modifier = Modifier.fillMaxSize()
                    )
                    EditorToolbar(
                        viewModel = viewModel,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 14.dp)
                    )
                }
            }
        }
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text(text = stringResource(R.string.editor_unsaved_title)) },
            text = { Text(text = stringResource(R.string.editor_unsaved_message)) },
            confirmButton = {
                Row {
                    TextButton(onClick = { showUnsavedDialog = false }) {
                        Text(text = stringResource(R.string.editor_cancel))
                    }
                    TextButton(
                        onClick = {
                            showUnsavedDialog = false
                            viewModel.save { outcome -> handleSaveOutcome(outcome) }
                        }
                    ) {
                        Text(text = stringResource(R.string.editor_save))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUnsavedDialog = false
                        viewModel.discardAndForget()
                        onBack()
                    }
                ) {
                    Text(text = stringResource(R.string.editor_discard))
                }
            }
        )
    }

    if (ui.saveError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSaveError() },
            title = { Text(text = stringResource(R.string.editor_save_failed_title)) },
            text = {
                Text(
                    text = stringResource(
                        when (ui.saveError) {
                            FileErrorKind.PERMISSION_LOST ->
                                R.string.editor_error_save_permission

                            FileErrorKind.NOT_FOUND ->
                                R.string.editor_error_save_not_found

                            else -> R.string.editor_error_save_generic
                        }
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSaveError() }) {
                    Text(text = stringResource(R.string.editor_ok))
                }
            }
        )
    }

    if (ui.externalChange) {
        AlertDialog(
            onDismissRequest = { viewModel.keepMineDespiteExternalChange() },
            title = { Text(text = stringResource(R.string.editor_external_title)) },
            text = { Text(text = stringResource(R.string.editor_external_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.reloadFromDisk() }) {
                    Text(text = stringResource(R.string.editor_reload))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.keepMineDespiteExternalChange() }
                ) {
                    Text(text = stringResource(R.string.editor_keep_mine))
                }
            }
        )
    }
}

@Composable
private fun EditorBody(
    state: TextFieldState,
    highlightEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val density = LocalDensity.current

    val transformation = if (highlightEnabled) {
        MarkdownHighlightTransformation(
            headingStyle = SpanStyle(fontWeight = FontWeight.Bold),
            codeStyle = SpanStyle(
                background = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.08f
                )
            ),
            fenceStyle = SpanStyle(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            ),
            linkStyle = SpanStyle(
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            ),
            markerStyle = SpanStyle(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        )
    } else {
        null
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.width(34.dp)) {
                val result = layout
                if (result != null) {
                    val text = state.text.toString()
                    val numbers = remember(result, text) {
                        val list = ArrayList<Int?>(result.lineCount)
                        var logical = 0
                        for (line in 0 until result.lineCount) {
                            val start = result.getLineStart(line)
                            val isLogicalStart = line == 0 ||
                                (start > 0 && text[start - 1] == '\n')
                            if (isLogicalStart) {
                                logical++
                                list.add(logical)
                            } else {
                                list.add(null)
                            }
                        }
                        list
                    }
                    numbers.forEachIndexed { line, number ->
                        Box(
                            modifier = Modifier.height(
                                with(density) {
                                    (result.getLineBottom(line) -
                                        result.getLineTop(line)).toDp()
                                }
                            )
                        ) {
                            Text(
                                text = number?.toString().orEmpty(),
                                style = TextStyle(
                                    fontFamily = MonoFamily,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme
                                        .onSurfaceVariant,
                                    textAlign = TextAlign.End
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                state = state,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontFamily = MonoFamily,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(
                    MaterialTheme.colorScheme.onSurface
                ),
                lineLimits = TextFieldLineLimits.MultiLine(),
                outputTransformation = transformation,
                onTextLayout = { getResult -> layout = getResult() }
            )
        }
    }
}

@Composable
private fun EditorToolbar(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DeepBlack,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            ToolButton(
                icon = Icons.Filled.FormatBold,
                description = stringResource(R.string.editor_bold)
            ) { viewModel.bold() }
            ToolButton(
                icon = Icons.Filled.FormatItalic,
                description = stringResource(R.string.editor_italic)
            ) { viewModel.italic() }
            ToolButton(
                icon = Icons.Filled.Title,
                description = stringResource(R.string.editor_heading)
            ) { viewModel.heading() }
            ToolButton(
                icon = Icons.Filled.Link,
                description = stringResource(R.string.editor_link)
            ) { viewModel.link() }
            ToolButton(
                icon = Icons.Filled.Code,
                description = stringResource(R.string.editor_code)
            ) { viewModel.code() }
            ToolButton(
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                description = stringResource(R.string.editor_list)
            ) { viewModel.list() }
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Cream
        )
    }
}
