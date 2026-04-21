package com.jetbrains.spacetutorial.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jetbrains.spacetutorial.R
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme

// ── Hero icon placeholder (replace drawable with your own asset) ──────────────

@Composable
fun OnboardingHeroIcon(modifier: Modifier = Modifier) {
    val colors = TexasWatchTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(colors.primaryAccent.copy(alpha = 0.12f)),
    ) {
        Icon(
            painter = painterResource(R.drawable.team_28),
            contentDescription = null,
            tint = colors.primaryAccent,
            modifier = Modifier.size(56.dp),
        )
    }
}

// ── Header bar used on the detail (full text) view ────────────────────────────

@Composable
fun OnboardingHeaderBar(
    title: String,
    startContent: @Composable () -> Unit = {},
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 4.dp),
    ) {
        startContent()
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = typography.h3,
            color = colors.primaryText,
        )
    }
}

// ── "Read more" action row ─────────────────────────────────────────────────────

@Composable
fun OnboardingActionRow(
    label: String,
    onClick: () -> Unit,
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, colors.strokePale, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = label,
            style = typography.text1,
            color = colors.primaryText,
        )
        Icon(
            painter = painterResource(R.drawable.arrow_left_24),
            contentDescription = null,
            tint = colors.primaryAccent,
            modifier = Modifier
                .size(20.dp)
                .scale(scaleX = -1f, scaleY = 1f),
        )
    }
}

// ── Primary / secondary button ────────────────────────────────────────────────

@Composable
fun OnboardingButton(
    label: String,
    onClick: () -> Unit,
    primary: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    val bgColor = if (primary) colors.primaryAccent else colors.mainBackground
    val textColor = if (primary) colors.invertedText else colors.primaryText
    val borderColor = if (primary) colors.primaryAccent else colors.strokePale

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = label,
            style = typography.h4,
            color = textColor,
        )
    }
}

// ── Toggle setting row ────────────────────────────────────────────────────────

@Composable
fun OnboardingToggleItem(
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, colors.strokePale, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = typography.h4,
                color = colors.primaryText,
            )
            Text(
                text = description,
                style = typography.text2,
                color = colors.secondaryText,
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.invertedText,
                checkedTrackColor = colors.primaryAccent,
                uncheckedThumbColor = colors.secondaryText,
                uncheckedTrackColor = colors.strokePale,
            ),
        )
    }
}
