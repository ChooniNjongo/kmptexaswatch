package com.jetbrains.spacetutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import com.jetbrains.spacetutorial.ui.MainHeaderTitleBar

@Composable
fun MapScreen() {
    val colors = TexasWatchTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.mainBackground)
    ) {
        MainHeaderTitleBar(title = "Map")

        HorizontalDivider(thickness = 1.dp, color = colors.strokePale)

        // Body — map coming soon
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.mainBackground),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Map coming soon",
                style = TexasWatchTheme.typography.text1,
                color = colors.secondaryText,
            )
        }
    }
}
