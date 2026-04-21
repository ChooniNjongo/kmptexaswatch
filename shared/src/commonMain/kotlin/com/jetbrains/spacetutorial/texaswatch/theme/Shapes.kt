package com.jetbrains.spacetutorial.texaswatch.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

class Shapes(
    val small: RoundedCornerShape,
    val medium: RoundedCornerShape,
    val large: RoundedCornerShape,
)

val TexasWatchShapes = Shapes(
    small  = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(12.dp),
    large  = RoundedCornerShape(20.dp),
)
