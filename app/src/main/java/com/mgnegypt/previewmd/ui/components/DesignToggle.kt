package com.mgnegypt.previewmd.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgnegypt.previewmd.ui.theme.AccentPink
import com.mgnegypt.previewmd.ui.theme.Cream
import com.mgnegypt.previewmd.ui.theme.Ink
import com.mgnegypt.previewmd.ui.theme.MutedInk
import com.mgnegypt.previewmd.ui.theme.PillShape
import com.mgnegypt.previewmd.ui.theme.PreviewMDTheme

/**
 * The design's switch: an ink track with a pink thumb when on. Material's
 * Switch cannot reproduce this, so the component is drawn directly; it
 * still exposes full switch semantics to accessibility services.
 */
@Composable
fun DesignToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 58.dp, height = 34.dp)
            .clip(PillShape)
            .background(if (checked) Ink else MutedInk.copy(alpha = 0.35f))
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .padding(4.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (checked) AccentPink else Cream)
        )
    }
}

@Preview(name = "Toggle Light", showBackground = true)
@Composable
private fun ToggleLightPreview() {
    PreviewMDTheme(darkTheme = false) {
        var on by remember { mutableStateOf(true) }
        Box(Modifier.padding(16.dp)) {
            DesignToggle(checked = on, onCheckedChange = { on = it })
        }
    }
}

@Preview(
    name = "Toggle Dark RTL",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "ar"
)
@Composable
private fun ToggleDarkRtlPreview() {
    PreviewMDTheme(darkTheme = true) {
        Box(Modifier.padding(16.dp)) {
            DesignToggle(checked = false, onCheckedChange = {})
        }
    }
}
