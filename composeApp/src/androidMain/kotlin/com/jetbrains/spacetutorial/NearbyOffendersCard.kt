package com.jetbrains.spacetutorial

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import kotlinx.coroutines.delay

private const val ANIM_DURATION_MS = 1200L

@Composable
fun NearbyOffendersCard(
    count: Int = 247,
    progress: Float = 1f,
    locationActive: Boolean = true,
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography

    val ringActive = colors.ringActive
    val ringTrack  = colors.ringTrack

    // ── Ring animation ────────────────────────────────────────────────────────
    var animatedTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(locationActive) {
        animatedTarget = if (locationActive) progress else 0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = animatedTarget,
        animationSpec = tween(durationMillis = ANIM_DURATION_MS.toInt()),
        label = "ringProgress",
    )

    // ── Count animation ───────────────────────────────────────────────────────
    var displayCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(locationActive, count) {
        if (!locationActive) { displayCount = 0; return@LaunchedEffect }
        val steps = count
        val intervalMs = ANIM_DURATION_MS / steps.coerceAtLeast(1)
        for (i in 1..steps) {
            delay(intervalMs)
            displayCount = i
        }
    }

    val ringSize = 88.dp
    val strokeWidth = 10.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
            .background(colors.cardBackground, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Left: text ────────────────────────────────────────────────────────
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = "Nearby Offenders",
                style = typography.text2,
                color = colors.secondaryText,
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "$displayCount",
                    style = typography.h1,
                    color = colors.primaryText,
                )
                Text(
                    text = "found",
                    style = typography.text2,
                    color = colors.secondaryText,
                )
            }
            Text(
                text = "within 5 mile radius",
                style = typography.text2,
                color = colors.secondaryText,
            )
        }

        Spacer(modifier = Modifier.size(16.dp))

        // ── Right: donut ring + icon ──────────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(ringSize)
                .drawBehind {
                    val stroke = strokeWidth.toPx()
                    val inset  = stroke / 2f
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    val topLeft = Offset(inset, inset)

                    // Track
                    drawArc(
                        color      = ringTrack,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter  = false,
                        topLeft    = topLeft,
                        size       = arcSize,
                        style      = Stroke(width = stroke, cap = StrokeCap.Round),
                    )

                    // Active arc
                    if (animatedProgress > 0f) {
                        drawArc(
                            color      = ringActive,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter  = false,
                            topLeft    = topLeft,
                            size       = arcSize,
                            style      = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                    }
                },
        ) {
            Icon(
                painter            = painterResource(R.drawable.location_28),
                contentDescription = "Location",
                tint               = if (locationActive) ringActive else ringTrack,
                modifier           = Modifier.size(24.dp),
            )
        }
    }
}
