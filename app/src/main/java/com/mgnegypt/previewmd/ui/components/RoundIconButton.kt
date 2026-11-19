package com.mgnegypt.previewmd.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgnegypt.previewmd.ui.theme.PreviewMDTheme

/** The design's round icon button (back, edit, theme toggle). */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Preview(name = "RoundIcon Light", showBackground = true)
@Composable
private fun RoundIconLightPreview() {
    PreviewMDTheme(darkTheme = false) {
        RoundIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "RoundIcon Dark RTL",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "ar"
)
@Composable
private fun RoundIconDarkRtlPreview() {
    PreviewMDTheme(darkTheme = true) {
        RoundIconButton(
            icon = Icons.Filled.LightMode,
            contentDescription = null,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
