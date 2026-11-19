package com.mgnegypt.previewmd.ui.viewer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgnegypt.previewmd.ui.theme.DeepBlack
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.highlightedCodeBlock
import com.mikepenz.markdown.compose.elements.highlightedCodeFence
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography

/**
 * Phase 4 SPIKE: renders one sample covering every GFM feature the plan
 * requires, using the library defaults plus the app theme, to establish
 * what works out of the box and what needs custom components. Findings are
 * recorded in docs/MARKDOWN_SUPPORT.md. Replaced by the real viewer in the
 * next step of this phase.
 */
@Composable
fun MarkdownSpikeScreen(modifier: Modifier = Modifier) {
    val colors = markdownColor(
        text = MaterialTheme.colorScheme.onBackground,
        codeBackground = DeepBlack,
        inlineCodeBackground = MaterialTheme.colorScheme.surface,
        dividerColor = MaterialTheme.colorScheme.surface,
        tableBackground = MaterialTheme.colorScheme.surface
    )
    val typography = markdownTypography(
        h1 = MaterialTheme.typography.headlineLarge,
        h2 = MaterialTheme.typography.headlineMedium,
        h3 = MaterialTheme.typography.titleLarge,
        text = MaterialTheme.typography.bodyLarge,
        paragraph = MaterialTheme.typography.bodyLarge,
        code = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = com.mgnegypt.previewmd.ui.theme.MonoFamily,
            color = com.mgnegypt.previewmd.ui.theme.Cream
        ),
        inlineCode = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = com.mgnegypt.previewmd.ui.theme.MonoFamily
        )
    )
    Markdown(
        content = SPIKE_SAMPLE,
        colors = colors,
        typography = typography,
        imageTransformer = Coil3ImageTransformerImpl,
        components = markdownComponents(
            codeFence = highlightedCodeFence,
            codeBlock = highlightedCodeBlock
        ),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    )
}

private val SPIKE_SAMPLE = """
# Heading 1

A paragraph with **bold**, *italic*, ~~strikethrough~~, `inline code`,
a [link](https://example.com) and an autolink https://github.com.

## Heading 2

> A blockquote with **bold** inside.

### Tasks

- [x] Render tables
- [ ] Export to PDF
- A plain bullet

1. First
2. Second

```kotlin
fun main() {
    println("Hello, Markdown")
}
```

| Feature | Status |
|---|---|
| Tables | Done |
| Images | Done |

---

![Badge](https://img.shields.io/badge/Preview_MD-spike-blue)

> [!NOTE]
> A GFM alert block.

""".trimIndent()
