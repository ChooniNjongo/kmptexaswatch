package com.jetbrains.spacetutorial.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme

// ── MainHeaderTitleBar ────────────────────────────────────────────────────────
// Mirrors KotlinConf MainHeaderTitleBar exactly:
//   48dp tall, centered title, optional start/end slots

@Composable
fun MainHeaderTitleBar(
    title: String,
    modifier: Modifier = Modifier,
    startContent: @Composable RowScope.() -> Unit = {},
    endContent: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth()
            .background(TexasWatchTheme.colors.mainBackground),
        contentAlignment = Alignment.Center,
    ) {
        Row(Modifier.align(Alignment.CenterStart)) {
            startContent()
        }
        Text(
            text = title,
            style = TexasWatchTheme.typography.h3,
            color = TexasWatchTheme.colors.primaryText,
        )
        Row(Modifier.align(Alignment.CenterEnd)) {
            endContent()
        }
    }
}
