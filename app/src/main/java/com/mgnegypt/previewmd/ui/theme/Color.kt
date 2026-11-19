package com.mgnegypt.previewmd.ui.theme

import androidx.compose.ui.graphics.Color

// Exact design tokens, extracted from the design PDF on 2026-11-19 by
// rendering all 12 pages at 150 dpi and counting flat-fill pixel colors.
// The same values appear on every page; do not replace them by eye.

// Surfaces and text
val Cream = Color(0xFFFBF4E4) // light background; text on dark surfaces
val Beige = Color(0xFFEFE5CC) // light surface: cards, fields, editor page
val Ink = Color(0xFF1A1A1A) // primary text; primary buttons in both themes
val DeepBlack = Color(0xFF0D0D0D) // code blocks, floating bars
val MutedInk = Color(0xFF5C5545) // secondary text on light
val DarkBackground = Color(0xFF17140F)
val DarkSurface = Color(0xFF2A261D) // dark surface: cards, fields, editor page
val MutedOnDark = Color(0xFFB6AF9E) // secondary text on dark

// Accents: identical in the light and dark designs
val AccentYellow = Color(0xFFF6DC6E)
val AccentPink = Color(0xFFF4B6DD)
val AccentBlue = Color(0xFFB6C8F0)
val AccentGreen = Color(0xFF9BB26A)

/** File cards cycle through the four accents in this order. */
val FileCardColors = listOf(AccentYellow, AccentPink, AccentBlue, AccentGreen)
