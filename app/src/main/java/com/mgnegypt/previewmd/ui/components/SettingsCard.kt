package com.mgnegypt.previewmd.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgnegypt.previewmd.ui.theme.AccentPink
import com.mgnegypt.previewmd.ui.theme.AccentYellow
import com.mgnegypt.previewmd.ui.theme.Cream
import com.mgnegypt.previewmd.ui.theme.Ink
import com.mgnegypt.previewmd.ui.theme.PreviewMDTheme

/** A large rounded card in one of the design's accent colors. */
@Composable
fun SettingsCard(
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = color,
        contentColor = Ink
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Preview(name = "SettingsCard Light", showBackground = true)
@Composable
private fun SettingsCardLightPreview() {
    PreviewMDTheme(darkTheme = false) {
        var on by remember { mutableStateOf(true) }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsCard(color = AccentYellow) {
                Text("Updates", style = MaterialTheme.typography.titleLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Automatic updates",
                        style = MaterialTheme.typography.titleMedium
                    )
                    DesignToggle(checked = on, onCheckedChange = { on = it })
                }
                SegmentedPill(
                    options = listOf("Stable", "Beta"),
                    selectedIndex = 0,
                    onSelected = {},
                    containerColor = Cream
                )
            }
            SettingsCard(color = AccentPink) {
                Text(
                    "Update 1.1.0 is available",
                    style = MaterialTheme.typography.titleMedium
                )
                ProgressPill(progress = 0.4f)
                PillButtonPrimary(text = "Download and install", onClick = {})
            }
        }
    }
}

@Preview(
    name = "SettingsCard Dark RTL",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "ar"
)
@Composable
private fun SettingsCardDarkRtlPreview() {
    PreviewMDTheme(darkTheme = true) {
        SettingsCard(
            color = AccentYellow,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("التحديثات", style = MaterialTheme.typography.titleLarge)
            ProgressPill(progress = 0.7f)
        }
    }
}
