package com.mgnegypt.previewmd.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgnegypt.previewmd.ui.theme.AccentPink
import com.mgnegypt.previewmd.ui.theme.Cream
import com.mgnegypt.previewmd.ui.theme.Ink
import com.mgnegypt.previewmd.ui.theme.PillShape
import com.mgnegypt.previewmd.ui.theme.PreviewMDTheme

/**
 * The design's full-width pill button. Colors are explicit parameters
 * because the design uses the same component in ink, surface and accent
 * variants; [PillButtonPrimary], [PillButtonSecondary] and
 * [PillButtonAccent] cover the drawn variants.
 */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = PillShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(20.dp)
                )
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge)
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(22.dp)
                )
            }
        }
    }
}

@Composable
fun PillButtonPrimary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) = PillButton(
    text = text,
    onClick = onClick,
    containerColor = Ink,
    contentColor = Cream,
    modifier = modifier,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon
)

@Composable
fun PillButtonSecondary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) = PillButton(
    text = text,
    onClick = onClick,
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
    modifier = modifier,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon
)

@Composable
fun PillButtonAccent(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) = PillButton(
    text = text,
    onClick = onClick,
    containerColor = AccentPink,
    contentColor = Ink,
    modifier = modifier,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon
)

@Preview(name = "PillButtons Light", showBackground = true)
@Composable
private fun PillButtonLightPreview() {
    PreviewMDTheme(darkTheme = false) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PillButtonPrimary(
                text = "Get started",
                onClick = {},
                trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight
            )
            PillButtonSecondary(text = "Open a file", onClick = {})
            PillButtonAccent(
                text = "Download and install",
                onClick = {},
                leadingIcon = Icons.Filled.Download
            )
        }
    }
}

@Preview(
    name = "PillButtons Dark RTL",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "ar"
)
@Composable
private fun PillButtonDarkRtlPreview() {
    PreviewMDTheme(darkTheme = true) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PillButtonPrimary(
                text = "ابدأ الآن",
                onClick = {},
                trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight
            )
            PillButtonSecondary(text = "افتح ملفا", onClick = {})
        }
    }
}
