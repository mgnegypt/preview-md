package com.mgnegypt.previewmd.ui.viewer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.mgnegypt.previewmd.R
import com.mgnegypt.previewmd.ui.theme.AccentGreen
import com.mgnegypt.previewmd.ui.theme.Cream
import com.mikepenz.markdown.compose.elements.MarkdownCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownCodeFence
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCode
import kotlinx.coroutines.delay
import org.intellij.markdown.ast.ASTNode

/**
 * Code blocks in the viewer: the library's syntax-highlighted rendering
 * with a GitHub-style copy button floating at the block's top end.
 * Tapping it copies the block's exact code (the same extraction the
 * renderer itself uses) to the system clipboard and flips the icon to
 * a check briefly. The button is a sibling of the highlighted text, so
 * it never scrolls away with long lines.
 */

@Composable
fun CopyableCodeFence(
    content: String,
    node: ASTNode,
    style: TextStyle
) {
    MarkdownCodeFence(content, node, style) { code, language, textStyle ->
        CopyableCode(code = code, language = language, style = textStyle)
    }
}

@Composable
fun CopyableCodeBlock(
    content: String,
    node: ASTNode,
    style: TextStyle
) {
    MarkdownCodeBlock(content, node, style) { code, language, textStyle ->
        CopyableCode(code = code, language = language, style = textStyle)
    }
}

@Composable
private fun CopyableCode(
    code: String,
    language: String?,
    style: TextStyle
) {
    Box {
        MarkdownHighlightedCode(code = code, language = language, style = style)
        CopyCodeButton(
            code = code,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 14.dp, end = 6.dp)
        )
    }
}

@Composable
private fun CopyCodeButton(
    code: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1600)
            copied = false
        }
    }
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .clickable {
                val clipboard = context.getSystemService(
                    Context.CLIPBOARD_SERVICE
                ) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
                copied = true
            }
            .semantics { role = Role.Button },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
            contentDescription = stringResource(R.string.viewer_copy_code),
            tint = if (copied) AccentGreen else Cream,
            modifier = Modifier.size(16.dp)
        )
    }
}
