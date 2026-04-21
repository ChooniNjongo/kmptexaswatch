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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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

private const val ANIM_DURATION_MS = 1200

@Composable
fun NearbyOffendersCard(
    count: Int = 0,
    progress: Float = 0f,
    locationActive: Boolean = false,
    isLoading: Boolean = false,
    onAllowLocation: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    val ringActive = colors.ringActive
    val ringTrack  = colors.ringTrack

    // Single animated fraction 0→1 drives BOTH ring sweep and count display
    var fractionTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(count, locationActive) {
        fractionTarget = if (locationActive && count > 0) 1f else 0f
    }
    val fraction by animateFloatAsState(
        targetValue = fractionTarget,
        animationSpec = tween(durationMillis = ANIM_DURATION_MS),
        label = "fraction",
    )

    val displayCount = (fraction * count).toInt()

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
        // ── Left ──────────────────────────────────────────────────────────────
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = "Nearby Offenders",
                style = typography.text2,
                color = colors.secondaryText,
            )

            if (!locationActive) {
                Button(
                    onClick = onAllowLocation,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.ringActive),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(
                        text = "Allow Location",
                        style = typography.label,
                        color = colors.invertedText,
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = if (isLoading) "..." else "$displayCount",
                        style = typography.h1,
                        color = colors.primaryText,
                    )
                    if (!isLoading) {
                        Text(
                            text = "found",
                            style = typography.text2,
                            color = colors.secondaryText,
                        )
                    }
                }
                Text(
                    text = "within radius",
                    style = typography.text2,
                    color = colors.secondaryText,
                )
            }
        }

        Spacer(modifier = Modifier.size(16.dp))

        // ── Right: donut ring ─────────────────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(ringSize)
                .drawBehind {
                    val stroke = strokeWidth.toPx()
                    val inset  = stroke / 2f
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    val topLeft = Offset(inset, inset)

                    drawArc(
                        color      = ringTrack,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter  = false,
                        topLeft    = topLeft,
                        size       = arcSize,
                        style      = Stroke(width = stroke, cap = StrokeCap.Round),
                    )

                    if (fraction > 0f) {
                        drawArc(
                            color      = ringActive,
                            startAngle = -90f,
                            sweepAngle = 360f * fraction,
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
