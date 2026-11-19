package com.mgnegypt.previewmd.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

// Fixed custom palette from the design file. Dynamic color is deliberately
// not used. In both themes the primary action is the ink pill with cream
// text, exactly as drawn; components take accent colors from Color.kt.
private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = Cream,
    primaryContainer = AccentYellow,
    onPrimaryContainer = Ink,
    secondary = AccentPink,
    onSecondary = Ink,
    secondaryContainer = Beige,
    onSecondaryContainer = Ink,
    tertiary = AccentBlue,
    onTertiary = Ink,
    tertiaryContainer = AccentGreen,
    onTertiaryContainer = Ink,
    background = Cream,
    onBackground = Ink,
    surface = Beige,
    onSurface = Ink,
    surfaceVariant = Beige,
    onSurfaceVariant = MutedInk,
    outline = MutedInk
)

private val DarkColors = darkColorScheme(
    primary = Ink,
    onPrimary = Cream,
    primaryContainer = AccentYellow,
    onPrimaryContainer = Ink,
    secondary = AccentPink,
    onSecondary = Ink,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = Cream,
    tertiary = AccentBlue,
    onTertiary = Ink,
    tertiaryContainer = AccentGreen,
    onTertiaryContainer = Ink,
    background = DarkBackground,
    onBackground = Cream,
    surface = DarkSurface,
    onSurface = Cream,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = MutedOnDark,
    outline = MutedOnDark
)

@Composable
fun PreviewMDTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = previewTypography(if (isArabic) CairoFamily else OutfitFamily),
        shapes = PreviewShapes,
        content = content
    )
}
