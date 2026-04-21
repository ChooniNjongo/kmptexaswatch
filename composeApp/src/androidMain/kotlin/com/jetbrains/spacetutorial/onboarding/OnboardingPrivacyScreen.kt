package com.jetbrains.spacetutorial.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jetbrains.spacetutorial.R
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import com.jetbrains.spacetutorial.ui.OnboardingActionRow
import com.jetbrains.spacetutorial.ui.OnboardingButton
import com.jetbrains.spacetutorial.ui.OnboardingHeaderBar
import com.jetbrains.spacetutorial.ui.OnboardingHeroIcon
import androidx.compose.material3.Text as M3Text

@Composable
fun OnboardingPrivacyScreen(
    onDecline: () -> Unit,
    onAccept: () -> Unit,
) {
    var detailsVisible by rememberSaveable { mutableStateOf(false) }
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        AnimatedContent(
            targetState = detailsVisible,
            modifier = Modifier.weight(1f),
        ) { showDetails ->
            if (showDetails) {
                // ── Full privacy text view ────────────────────────────────────
                Column(modifier = Modifier.fillMaxSize()) {
                    OnboardingHeaderBar(
                        title = "Privacy Notice",
                        startContent = {
                            IconButton(onClick = { detailsVisible = false }) {
                                Icon(
                                    painter = painterResource(R.drawable.arrow_left_24),
                                    contentDescription = "Back",
                                    tint = colors.primaryText,
                                )
                            }
                        }
                    )
                    HorizontalDivider(color = colors.strokePale, thickness = 1.dp)
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        M3Text(
                            text = privacyNoticeText,
                            style = typography.text1,
                            color = colors.secondaryText,
                        )
                    }
                }
            } else {
                // ── Summary view ──────────────────────────────────────────────
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                ) {
                    OnboardingHeroIcon()
                    M3Text(
                        text = "Privacy Notice",
                        style = typography.h1,
                        color = colors.primaryText,
                    )
                    M3Text(
                        text = "This app collects location data to find registered sex offenders near you. " +
                                "We do not sell or share your personal data with third parties. " +
                                "All location lookups happen on your device and are not stored on our servers.",
                        style = typography.text1,
                        color = colors.secondaryText,
                    )
                    OnboardingActionRow(
                        label = "Read full privacy notice",
                        onClick = { detailsVisible = true },
                    )
                }
            }
        }

        HorizontalDivider(color = colors.strokePale, thickness = 1.dp)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            OnboardingButton(
                label = "Decline",
                onClick = onDecline,
                primary = false,
                modifier = Modifier.weight(1f),
            )
            OnboardingButton(
                label = "Accept",
                onClick = onAccept,
                primary = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private val privacyNoticeText = """
**Texas Watch – Privacy Notice**

Last updated: 2026

**What data we collect**
Texas Watch uses your device location (when you grant permission) solely to find registered sex offenders near you. Location data is processed on-device and is never transmitted to our servers.

**How we use it**
Your location is used only to query our offender database API for results relevant to your area. We do not build location history or profiles.

**Third-party services**
We use no third-party analytics, advertising networks, or tracking SDKs.

**Your rights**
You may revoke location permission at any time in your device Settings. Revoking permission will limit search functionality to manual address or name searches.

**Contact**
For questions, contact support@texaswatch.app
""".trimIndent()
