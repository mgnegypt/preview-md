package com.mgnegypt.previewmd.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mgnegypt.previewmd.ui.theme.AccentGreen
import com.mgnegypt.previewmd.ui.theme.AccentYellow
import com.mgnegypt.previewmd.ui.theme.Cream
import com.mgnegypt.previewmd.ui.theme.DeepBlack
import com.mgnegypt.previewmd.ui.theme.MonoFamily
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownCheckBox
import com.mikepenz.markdown.compose.elements.MarkdownHeader
import com.mikepenz.markdown.compose.elements.MarkdownTable
import com.mikepenz.markdown.compose.elements.MarkdownTableHeader
import com.mikepenz.markdown.compose.elements.highlightedCodeBlock
import com.mikepenz.markdown.compose.elements.highlightedCodeFence
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography

/**
 * The viewer's Markdown styling: library defaults recolored to the app
 * palette, plus the design's custom pieces (heading dividers, drawn
 * task checkboxes, yellow table header). See docs/MARKDOWN_SUPPORT.md.
 */

@Composable
fun viewerMarkdownColors(): MarkdownColors = markdownColor(
    text = MaterialTheme.colorScheme.onBackground,
    codeBackground = DeepBlack,
    inlineCodeBackground = MaterialTheme.colorScheme.surface,
    dividerColor = MaterialTheme.colorScheme.surface,
    tableBackground = MaterialTheme.colorScheme.surface
)

@Composable
fun viewerMarkdownTypography(): MarkdownTypography = markdownTypography(
    h1 = MaterialTheme.typography.headlineLarge,
    h2 = MaterialTheme.typography.headlineMedium,
    h3 = MaterialTheme.typography.titleLarge,
    text = MaterialTheme.typography.bodyLarge,
    paragraph = MaterialTheme.typography.bodyLarge,
    code = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = MonoFamily,
        color = Cream
    ),
    inlineCode = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = MonoFamily
    ),
    textLink = TextLinkStyles(
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline
        ).toSpanStyle()
    )
)

@Composable
fun viewerMarkdownComponents(imageLinks: Map<String, String>) = markdownComponents(
    image = { model ->
        MarkdownBlockImage(
            content = model.content,
            node = model.node,
            imageLinks = imageLinks
        )
    },
    inlineImage = { model ->
        MarkdownInlineImage(link = model.content, imageLinks = imageLinks)
    },
    heading1 = { model ->
        Column {
            MarkdownHeader(model.content, model.node, model.typography.h1)
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    },
    heading2 = { model ->
        Column {
            MarkdownHeader(model.content, model.node, model.typography.h2)
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    },
    checkbox = { model ->
        MarkdownCheckBox(
            content = model.content,
            node = model.node,
            style = model.typography.text,
            checkedIndicator = { checked, modifier ->
                Box(
                    modifier = modifier
                        .size(20.dp)
                        .background(
                            color = if (checked) AccentGreen else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (checked) {
                                AccentGreen
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            shape = RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (checked) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = DeepBlack,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        )
    },
    table = { model ->
        MarkdownTable(
            content = model.content,
            node = model.node,
            style = model.typography.table,
            headerBlock = { content, header, tableWidth, style ->
                Box(
                    modifier = Modifier
                        .width(tableWidth)
                        .background(AccentYellow)
                ) {
                    MarkdownTableHeader(
                        content = content,
                        header = header,
                        tableWidth = tableWidth,
                        style = style
                    )
                }
            }
        )
    },
    codeFence = highlightedCodeFence,
    codeBlock = highlightedCodeBlock
)
