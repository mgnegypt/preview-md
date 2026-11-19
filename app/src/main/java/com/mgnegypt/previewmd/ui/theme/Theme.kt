package com.mgnegypt.previewmd.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Minimal seed theme for the Phase 0 bootstrap. Phase 1 builds the full
// design system (exact colors, typography, shapes) on top of this shape.
// Dynamic color is deliberately not used: the design has a fixed palette.
private val LightColors = lightColorScheme(
    primary = Ink,
    background = CreamBackground,
    surface = BeigeSurface,
    onPrimary = CreamBackground,
    onBackground = Ink,
    onSurface = Ink
)

private val DarkColors = darkColorScheme(
    primary = DarkAccentYellow,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkBackground,
    onBackground = CreamBackground,
    onSurface = CreamBackground
)

@Composable
fun PreviewMDTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
