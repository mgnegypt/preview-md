package com.mgnegypt.previewmd.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgnegypt.previewmd.ui.theme.AccentPink
import com.mgnegypt.previewmd.ui.theme.Cream
import com.mgnegypt.previewmd.ui.theme.Ink
import com.mgnegypt.previewmd.ui.theme.PillShape
import com.mgnegypt.previewmd.ui.theme.PreviewMDTheme

/** The design's pill progress bar (ink progress on a cream track). */
@Composable
fun ProgressPill(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = Cream,
    progressColor: Color = Ink
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(PillShape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(PillShape)
                .background(progressColor)
        )
    }
}

@Preview(name = "Progress Light", showBackground = true)
@Composable
private fun ProgressLightPreview() {
    PreviewMDTheme(darkTheme = false) {
        Box(
            Modifier
                .background(AccentPink)
                .padding(16.dp)
        ) {
            ProgressPill(progress = 0.4f)
        }
    }
}

@Preview(
    name = "Progress Dark RTL",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "ar"
)
@Composable
private fun ProgressDarkRtlPreview() {
    PreviewMDTheme(darkTheme = true) {
        Box(
            Modifier
                .background(AccentPink)
                .padding(16.dp)
        ) {
            ProgressPill(progress = 0.7f)
        }
    }
}
