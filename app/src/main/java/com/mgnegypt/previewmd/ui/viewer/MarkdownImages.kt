package com.mgnegypt.previewmd.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import com.mgnegypt.previewmd.R
import com.mikepenz.markdown.compose.LocalReferenceLinkHandler
import com.mikepenz.markdown.utils.resolveImageAlt
import com.mikepenz.markdown.utils.resolveImageLink
import org.intellij.markdown.ast.ASTNode

/**
 * The viewer's image rendering. The renderer library's default image
 * elements paint through an AsyncImagePainter whose loading failure is
 * invisible (an empty box), so the app renders images itself with Coil's
 * SubcomposeAsyncImage against the app-wide image loader: a loading
 * placeholder carrying the alt text, the image at its natural aspect on
 * success, and a visible unavailable placeholder on error. An image
 * never fails silently.
 */

/** A block-level image: full content width, natural aspect. */
@Composable
fun MarkdownBlockImage(
    content: String,
    node: ASTNode,
    imageLinks: Map<String, String>,
    modifier: Modifier = Modifier
) {
    val link = node.resolveImageLink(content, LocalReferenceLinkHandler.current) ?: return
    MarkdownAsyncImage(
        url = imageLinks[link] ?: link,
        alt = node.resolveImageAlt(content),
        contentScale = ContentScale.FillWidth,
        modifier = modifier.fillMaxWidth(),
        placeholderModifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    )
}

/** An image inside a paragraph: fills the slot the text layout gives it. */
@Composable
fun MarkdownInlineImage(
    link: String,
    imageLinks: Map<String, String>,
    modifier: Modifier = Modifier
) {
    MarkdownAsyncImage(
        url = imageLinks[link] ?: link,
        alt = null,
        contentScale = ContentScale.Fit,
        modifier = modifier.fillMaxSize(),
        placeholderModifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun MarkdownAsyncImage(
    url: String,
    alt: String?,
    contentScale: ContentScale,
    modifier: Modifier,
    placeholderModifier: Modifier
) {
    val context = LocalContext.current
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context).data(url).build(),
        imageLoader = SingletonImageLoader.get(context),
        contentDescription = alt,
        contentScale = contentScale,
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        loading = {
            ImagePlaceholder(alt = alt, failed = false, modifier = placeholderModifier)
        },
        error = {
            ImagePlaceholder(alt = alt, failed = true, modifier = placeholderModifier)
        }
    )
}

@Composable
private fun ImagePlaceholder(alt: String?, failed: Boolean, modifier: Modifier) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (failed) {
                Icon(
                    imageVector = Icons.Filled.BrokenImage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
            }
            val label = alt?.takeIf { it.isNotBlank() }
                ?: if (failed) stringResource(R.string.viewer_image_unavailable) else null
            if (label != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}
