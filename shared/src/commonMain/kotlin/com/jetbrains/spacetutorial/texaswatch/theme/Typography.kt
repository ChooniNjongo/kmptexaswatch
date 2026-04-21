package com.jetbrains.spacetutorial.texaswatch.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import spacetutorial.shared.generated.resources.Res
import spacetutorial.shared.generated.resources.jetbrains_sans_bold
import spacetutorial.shared.generated.resources.jetbrains_sans_regular
import spacetutorial.shared.generated.resources.jetbrains_sans_semibold
import org.jetbrains.compose.resources.Font

class Typography(
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val text1: TextStyle,
    val text2: TextStyle,
    val label: TextStyle,
)

internal val TexasWatchTypography: Typography
    @Composable
    get() = Typography(
        h1 = TextStyle(
            fontFamily = JetBrainsSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 34.sp,
        ),
        h2 = TextStyle(
            fontFamily = JetBrainsSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        ),
        h3 = TextStyle(
            fontFamily = JetBrainsSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        h4 = TextStyle(
            fontFamily = JetBrainsSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 20.sp,
        ),
        text1 = TextStyle(
            fontFamily = JetBrainsSans,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        text2 = TextStyle(
            fontFamily = JetBrainsSans,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 20.sp,
        ),
        label = TextStyle(
            fontFamily = JetBrainsSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            lineHeight = 16.sp,
        ),
    )

internal val JetBrainsSans: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.jetbrains_sans_bold, FontWeight.Bold, FontStyle.Normal),
        Font(Res.font.jetbrains_sans_semibold, FontWeight.SemiBold, FontStyle.Normal),
        Font(Res.font.jetbrains_sans_regular, FontWeight.Normal, FontStyle.Normal),
    )
