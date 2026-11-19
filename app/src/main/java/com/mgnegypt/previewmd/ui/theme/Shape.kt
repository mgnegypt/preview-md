package com.mgnegypt.previewmd.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// The design is built on large rounded shapes: full pills for buttons and
// bars, and generous corners on cards. Radii follow the rendered design.
val PreviewShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp), // file icon chip
    medium = RoundedCornerShape(20.dp), // code blocks, social buttons
    large = RoundedCornerShape(28.dp), // file cards, settings cards
    extraLarge = RoundedCornerShape(36.dp)
)

/** Full pill, used by buttons, segmented controls and floating bars. */
val PillShape = RoundedCornerShape(percent = 50)
