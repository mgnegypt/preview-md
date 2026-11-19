package com.mgnegypt.previewmd.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgnegypt.previewmd.ui.theme.AccentBlue
import com.mgnegypt.previewmd.ui.theme.AccentYellow
import com.mgnegypt.previewmd.ui.theme.Cream
import com.mgnegypt.previewmd.ui.theme.Ink
import com.mgnegypt.previewmd.ui.theme.MutedInk
import com.mgnegypt.previewmd.ui.theme.PreviewMDTheme

/**
 * The Home file card: an accent-colored rounded card with an ink icon chip,
 * the file name, its folder path and a relative time label at the end.
 */
@Composable
fun FileCard(
    name: String,
    path: String,
    timeLabel: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = color,
        contentColor = Ink
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Ink,
                contentColor = Cream,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 12.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = path,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = timeLabel,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Preview(name = "FileCard Light", showBackground = true)
@Composable
private fun FileCardLightPreview() {
    PreviewMDTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FileCard(
                name = "README.md",
                path = "github/preview-md",
                timeLabel = "2 min",
                color = AccentYellow,
                onClick = {}
            )
            FileCard(
                name = "NOTES.md",
                path = "downloads",
                timeLabel = "Yesterday",
                color = AccentBlue,
                onClick = {}
            )
        }
    }
}

@Preview(
    name = "FileCard Dark RTL",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "ar"
)
@Composable
private fun FileCardDarkRtlPreview() {
    PreviewMDTheme(darkTheme = true) {
        FileCard(
            name = "README.md",
            path = "github/preview-md",
            timeLabel = "قبل ساعتين",
            color = AccentYellow,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
