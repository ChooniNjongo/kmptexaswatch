package com.jetbrains.spacetutorial.texaswatch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderSummary
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme

// ── Offender Card ─────────────────────────────────────────────────────────────
// Same layout as SpeakerCard: 96dp photo on the left, name + subtitle on right

@Composable
fun OffenderCard(
    offender: OffenderSummary,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(TexasWatchTheme.shapes.medium)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .background(TexasWatchTheme.colors.cardBackground)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OffenderPhoto(
            photoUrl = offender.photoUrl,
            initials = initials(offender.firstName, offender.lastName),
            modifier = Modifier.size(96.dp),
        )
        Column {
            Text(
                text = offender.fullName,
                style = TexasWatchTheme.typography.h3,
                color = TexasWatchTheme.colors.primaryText,
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text = offenderSubtitle(offender),
                style = TexasWatchTheme.typography.text2,
                color = TexasWatchTheme.colors.secondaryText,
            )
            Spacer(Modifier.size(4.dp))
            if (!offender.address.isNullOrBlank()) {
                Text(
                    text = offender.address.orEmpty(),
                    style = TexasWatchTheme.typography.text2,
                    color = TexasWatchTheme.colors.secondaryText,
                )
            }
        }
    }
}

// ── Offender Photo ────────────────────────────────────────────────────────────
// Uses Coil SubcomposeAsyncImage so we can show InitialsAvatar in loading/error states

@Composable
fun OffenderPhoto(
    photoUrl: String?,
    initials: String,
    modifier: Modifier = Modifier,
) {
    val clippedModifier = modifier
        .clip(TexasWatchTheme.shapes.medium)
        .background(TexasWatchTheme.colors.surfaceBackground)

    if (!photoUrl.isNullOrBlank()) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = clippedModifier,
            contentScale = ContentScale.Crop,
            loading = { InitialsAvatar(initials = initials, modifier = Modifier.fillMaxSize()) },
            error = { InitialsAvatar(initials = initials, modifier = Modifier.fillMaxSize()) },
        )
    } else {
        InitialsAvatar(initials = initials, modifier = clippedModifier)
    }
}

// ── Initials fallback avatar ──────────────────────────────────────────────────

@Composable
fun InitialsAvatar(initials: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(TexasWatchTheme.shapes.medium)
            .background(TexasWatchTheme.colors.primaryAccent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = TexasWatchTheme.typography.h2,
            color = TexasWatchTheme.colors.invertedText,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun initials(firstName: String, lastName: String): String {
    val f = firstName.trim().firstOrNull()?.uppercaseChar() ?: ""
    val l = lastName.trim().firstOrNull()?.uppercaseChar() ?: ""
    return "$f$l"
}

private fun offenderSubtitle(offender: OffenderSummary): String {
    val parts = mutableListOf<String>()
    if (offender.age != null) parts.add("Age ${offender.age}")
    parts.add("DPS: ${offender.dpsNumber}")
    return parts.joinToString("  ·  ")
}
