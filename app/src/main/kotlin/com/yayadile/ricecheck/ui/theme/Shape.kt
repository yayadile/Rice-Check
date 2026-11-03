package com.yayadile.ricecheck.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Shape tokens from docs/design-tokens.md. */
val PillShape = RoundedCornerShape(percent = 50)
val CardShape = RoundedCornerShape(12.dp)
val CardLargeShape = RoundedCornerShape(16.dp)
val ViewfinderShape = RoundedCornerShape(24.dp)

val RiceShapes = Shapes(
    extraSmall = CardShape,
    small = CardShape,
    medium = CardLargeShape,
    large = CardLargeShape,
    extraLarge = PillShape
)
