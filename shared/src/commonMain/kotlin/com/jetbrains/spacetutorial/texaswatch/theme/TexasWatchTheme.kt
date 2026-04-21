package com.jetbrains.spacetutorial.texaswatch.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

val LocalColors = compositionLocalOf<Colors> {
    error("TexasWatchTheme must be in the call hierarchy to provide colors")
}

val LocalShapes = compositionLocalOf<Shapes> {
    error("TexasWatchTheme must be in the call hierarchy to provide shapes")
}

val LocalTypography = compositionLocalOf<Typography> {
    error("TexasWatchTheme must be in the call hierarchy to provide typography")
}

object TexasWatchTheme {
    val colors: Colors
        @Composable @ReadOnlyComposable
        get() = LocalColors.current

    val shapes: Shapes
        @Composable @ReadOnlyComposable
        get() = LocalShapes.current

    val typography: Typography
        @Composable @ReadOnlyComposable
        get() = LocalTypography.current
}

@Composable
fun TexasWatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) TexasWatchDarkColors else TexasWatchLightColors
    val typography = TexasWatchTypography
    val shapes = TexasWatchShapes

    CompositionLocalProvider(
        LocalColors provides colors,
        LocalShapes provides shapes,
        LocalTypography provides typography,
    ) {
        content()
    }
}
