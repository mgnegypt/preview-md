package com.mgnegypt.previewmd.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgnegypt.previewmd.ui.theme.Cream
import com.mgnegypt.previewmd.ui.theme.Ink
import com.mgnegypt.previewmd.ui.theme.PillShape
import com.mgnegypt.previewmd.ui.theme.PreviewMDTheme

/**
 * The design's segmented control: a pill track with an ink pill for the
 * selected option. On colored cards the track switches to cream, which the
 * caller passes as [containerColor].
 */
@Composable
fun SegmentedPill(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = modifier.height(56.dp),
        shape = PillShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Surface(
                    onClick = { onSelected(index) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = PillShape,
                    color = if (selected) Ink else Color.Transparent,
                    contentColor = if (selected) Cream else contentColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Segmented Light", showBackground = true)
@Composable
private fun SegmentedLightPreview() {
    PreviewMDTheme(darkTheme = false) {
        var selected by remember { mutableIntStateOf(0) }
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SegmentedPill(
                options = listOf("Recent", "Folders"),
                selectedIndex = selected,
                onSelected = { selected = it }
            )
            SegmentedPill(
                options = listOf("Light", "Dark", "System"),
                selectedIndex = selected,
                onSelected = { selected = it }
            )
        }
    }
}

@Preview(
    name = "Segmented Dark RTL",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "ar"
)
@Composable
private fun SegmentedDarkRtlPreview() {
    PreviewMDTheme(darkTheme = true) {
        SegmentedPill(
            options = listOf("العربية", "English"),
            selectedIndex = 0,
            onSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
