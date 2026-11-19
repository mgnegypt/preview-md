package com.mgnegypt.previewmd.ui.viewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mgnegypt.previewmd.R
import com.mgnegypt.previewmd.data.files.FileErrorKind
import com.mgnegypt.previewmd.ui.components.PillButtonPrimary
import com.mgnegypt.previewmd.ui.components.RoundIconButton
import com.mgnegypt.previewmd.ui.components.SegmentedPill
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.ImageData
import com.mikepenz.markdown.model.ImageTransformer
import kotlinx.coroutines.launch

/**
 * The document viewer (design screen 4): file name header, the
 * Preview|Edit segmented control and the GitHub-style rendering.
 * Link behaviour: http(s)/mailto go to the system handler, a resolvable
 * relative .md link opens in the app, #anchors are accepted without
 * scrolling in this version (documented in docs/MARKDOWN_SUPPORT.md).
 */
@Composable
fun ViewerScreen(
    viewModel: ViewerViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenLinkedDocument: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            RoundIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                onClick = onBack
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = (state as? ViewerUiState.Content)?.fileName.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            RoundIconButton(
                icon = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.viewer_edit),
                onClick = onEdit
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            SegmentedPill(
                options = listOf(
                    stringResource(R.string.viewer_preview),
                    stringResource(R.string.viewer_edit)
                ),
                selectedIndex = 0,
                onSelected = { index -> if (index == 1) onEdit() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (val current = state) {
            ViewerUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            is ViewerUiState.Error -> {
                ViewerError(
                    kind = current.kind,
                    onRetry = { viewModel.load() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            is ViewerUiState.Content -> {
                if (current.markdown.isBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.viewer_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    ViewerContent(
                        content = current,
                        viewModel = viewModel,
                        onOpenLinkedDocument = onOpenLinkedDocument,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewerContent(
    content: ViewerUiState.Content,
    viewModel: ViewerViewModel,
    onOpenLinkedDocument: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultUriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val uriHandler = remember(viewModel, defaultUriHandler) {
        object : UriHandler {
            override fun openUri(uri: String) {
                when {
                    uri.startsWith("#") -> Unit
                    uri.startsWith("http://") ||
                        uri.startsWith("https://") ||
                        uri.startsWith("mailto:") ->
                        defaultUriHandler.openUri(uri)

                    uri.startsWith("content://") || uri.startsWith("file://") ->
                        onOpenLinkedDocument(uri)

                    else -> scope.launch {
                        viewModel.resolveLink(uri)
                            ?.let { onOpenLinkedDocument(it) }
                    }
                }
            }
        }
    }
    val imageTransformer = remember(content.imageLinks) {
        object : ImageTransformer {
            @Composable
            override fun transform(link: String): ImageData? {
                return Coil3ImageTransformerImpl.transform(
                    content.imageLinks[link] ?: link
                )
            }

            @Composable
            override fun intrinsicSize(painter: Painter): Size {
                return Coil3ImageTransformerImpl.intrinsicSize(painter)
            }
        }
    }

    CompositionLocalProvider(LocalUriHandler provides uriHandler) {
        Markdown(
            content = content.markdown,
            colors = viewerMarkdownColors(),
            typography = viewerMarkdownTypography(),
            components = viewerMarkdownComponents(),
            imageTransformer = imageTransformer,
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun ViewerError(
    kind: FileErrorKind,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(32.dp)
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
                when (kind) {
                    FileErrorKind.PERMISSION_LOST -> R.string.viewer_error_permission
                    FileErrorKind.NOT_FOUND -> R.string.viewer_error_not_found
                    FileErrorKind.IO_FAILURE -> R.string.viewer_error_generic
                }
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        PillButtonPrimary(
            text = stringResource(R.string.viewer_retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
