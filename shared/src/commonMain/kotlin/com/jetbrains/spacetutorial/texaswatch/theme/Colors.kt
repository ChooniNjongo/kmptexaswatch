package com.jetbrains.spacetutorial.texaswatch.theme

import androidx.compose.ui.graphics.Color

data class Colors(
    val isDark: Boolean,

    val mainBackground: Color,
    val surfaceBackground: Color,
    val cardBackground: Color,

    val primaryAccent: Color,
    val secondaryAccent: Color,

    val strokeFull: Color,
    val strokePale: Color,

    val primaryText: Color,
    val secondaryText: Color,
    val invertedText: Color,
    val accentText: Color,
    val dangerText: Color,
    val successText: Color,

    val dangerBadge: Color,
    val successBadge: Color,
    val neutralBadge: Color,

    val ringActive: Color,
    val ringTrack: Color,
)

val TexasWatchLightColors = Colors(
    isDark = false,

    mainBackground    = UI.white100,
    surfaceBackground = UI.grey100,
    cardBackground    = UI.white100,

    primaryAccent     = Brand.navy100,
    secondaryAccent   = Brand.red100,

    strokeFull        = UI.black100,
    strokePale        = UI.black15,

    primaryText       = UI.black100,
    secondaryText     = UI.black60,
    invertedText      = UI.white100,
    accentText        = Brand.navy100,
    dangerText        = Brand.danger,
    successText       = Brand.success,

    dangerBadge       = Brand.danger,
    successBadge      = Brand.success,
    neutralBadge      = UI.grey500,

    ringActive        = Brand.purple,
    ringTrack         = UI.black15,
)

val TexasWatchDarkColors = Colors(
    isDark = true,

    mainBackground    = UI.black100,
    surfaceBackground = UI.grey900,
    cardBackground    = Color(0xFF2A2A2D),

    primaryAccent     = Brand.navy80,
    secondaryAccent   = Brand.red80,

    strokeFull        = UI.white100,
    strokePale        = UI.white20,

    primaryText       = UI.white100,
    secondaryText     = UI.white70,
    invertedText      = UI.black100,
    accentText        = Color(0xFF7986CB),
    dangerText        = Brand.dangerDark,
    successText       = Brand.successDark,

    dangerBadge       = Brand.dangerDark,
    successBadge      = Brand.successDark,
    neutralBadge      = UI.grey400,

    ringActive        = Brand.purpleDark,
    ringTrack         = UI.white20,
)
