package com.jetbrains.spacetutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import com.jetbrains.spacetutorial.ui.MainHeaderTitleBar

// ── Offenders Snap & Search ───────────────────────────────────────────────────
// Home screen following the KotlinConf screen template:
//   - MainHeaderTitleBar (48dp) with centered title
//   - HorizontalDivider
//   - Body (to be designed later)

@Composable
fun OffendersSnapAndSearchScreen() {
    val colors = TexasWatchTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.mainBackground)
    ) {
        MainHeaderTitleBar(title = "Offenders")

        HorizontalDivider(
            thickness = 1.dp,
            color = colors.strokePale,
        )

        NearbyOffendersCard(
            count = 247,
            progress = 1f,
            locationActive = true,
        )
    }
}
