package com.jetbrains.spacetutorial

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import com.jetbrains.spacetutorial.ui.MainHeaderTitleBar

// ── Theme enum ────────────────────────────────────────────────────────────────

enum class AppTheme { SYSTEM, LIGHT, DARK }

// ── Settings screen ───────────────────────────────────────────────────────────

@Composable
fun SettingsScreen() {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography

    var selectedTheme by remember { mutableStateOf(AppTheme.SYSTEM) }
    var nearbyAlerts by remember { mutableStateOf(true) }
    var appUpdates by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.mainBackground),
    ) {
        MainHeaderTitleBar(title = "Settings")
        HorizontalDivider(thickness = 1.dp, color = colors.strokePale)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            // ── Theme section ─────────────────────────────────────────────────
            SettingsSectionHeading("Appearance")
            ThemeSelector(
                selected = selectedTheme,
                onSelect = { selectedTheme = it },
            )

            Spacer(Modifier.height(16.dp))

            // ── Notifications section ─────────────────────────────────────────
            SettingsSectionHeading("Notifications")
            SettingsToggleItem(
                title = "Nearby Alerts",
                note = "Get notified when a new offender is registered near you.",
                enabled = nearbyAlerts,
                onToggle = { nearbyAlerts = it },
            )
            SettingsToggleItem(
                title = "App Updates",
                note = "Receive updates about new features and data improvements.",
                enabled = appUpdates,
                onToggle = { appUpdates = it },
            )

            Spacer(Modifier.height(16.dp))

            // ── About section ─────────────────────────────────────────────────
            SettingsSectionHeading("About")
            SettingsMenuItem(title = "Privacy Notice", onClick = {})
            SettingsMenuItem(title = "Terms of Use", onClick = {})
            SettingsMenuItem(title = "Licenses", onClick = {})

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Texas Watch v1.0.0",
                style = typography.text2,
                color = colors.secondaryText,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Section heading ───────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeading(text: String) {
    Text(
        text = text,
        style = TexasWatchTheme.typography.h2,
        color = TexasWatchTheme.colors.primaryText,
        modifier = Modifier
            .semantics { heading() }
            .padding(top = 8.dp, bottom = 4.dp),
    )
}

// ── Theme selector — 3 boxes ──────────────────────────────────────────────────

@Composable
private fun ThemeSelector(
    selected: AppTheme,
    onSelect: (AppTheme) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.selectableGroup(),
    ) {
        AppTheme.entries.forEach { theme ->
            ThemeBox(
                theme = theme,
                isSelected = selected == theme,
                onClick = { onSelect(theme) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ThemeBox(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.selectable(
            selected = isSelected,
            onClick = onClick,
            role = Role.RadioButton,
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
        ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .then(
                    if (isSelected) Modifier.border(
                        width = 2.dp,
                        color = colors.primaryAccent,
                        shape = RoundedCornerShape(20.dp),
                    ) else Modifier
                )
                .padding(6.dp)
                .border(
                    width = 2.dp,
                    color = colors.strokePale,
                    shape = RoundedCornerShape(12.dp),
                )
                .clip(RoundedCornerShape(12.dp))
                .heightIn(max = 112.dp)
                .aspectRatio(1f)
                .background(colors.surfaceBackground),
        ) {
            // Theme preview — colored block representing light/dark/system
            ThemePreviewContent(theme)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = when (theme) {
                AppTheme.SYSTEM -> "System"
                AppTheme.LIGHT -> "Light"
                AppTheme.DARK -> "Dark"
            },
            style = typography.text2,
            color = colors.primaryText,
        )
    }
}

@Composable
private fun ThemePreviewContent(theme: AppTheme) {
    val colors = TexasWatchTheme.colors
    when (theme) {
        AppTheme.SYSTEM -> {
            // Half light / half dark
            Row(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxSize().background(colors.mainBackground))
                Box(Modifier.weight(1f).fillMaxSize().background(colors.strokeFull))
            }
        }
        AppTheme.LIGHT -> {
            Box(Modifier.fillMaxSize().background(colors.invertedText))
        }
        AppTheme.DARK -> {
            Box(Modifier.fillMaxSize().background(colors.strokeFull))
        }
    }
}

// ── Settings toggle row (mirrors KotlinConf SettingsItem) ─────────────────────

@Composable
private fun SettingsToggleItem(
    title: String,
    note: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceBackground)
            .toggleable(
                value = enabled,
                role = Role.Switch,
                onValueChange = { onToggle(!enabled) },
                interactionSource = interactionSource,
                indication = null,
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(text = title, style = typography.h3, color = colors.primaryText)
            Text(text = note, style = typography.text2, color = colors.secondaryText)
        }
        KotlinConfToggle(enabled = enabled, onToggle = onToggle)
    }
}

// ── Custom toggle (mirrors KotlinConf Toggle.kt) ──────────────────────────────

@Composable
private fun KotlinConfToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val colors = TexasWatchTheme.colors
    val toggleOn = colors.primaryAccent
    val toggleOff = colors.strokePale

    val trackColor by animateColorAsState(if (enabled) toggleOn else toggleOff, label = "track")
    val thumbOffset by animateDpAsState(if (enabled) 7.dp else (-7).dp, label = "thumb")
    val thumbCenter = colors.mainBackground

    Box(contentAlignment = Alignment.Center) {
        // Track
        Box(
            Modifier
                .size(28.dp, 16.dp)
                .clip(RoundedCornerShape(percent = 100))
                .background(trackColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onToggle(!enabled) },
                )
        )
        // Thumb
        Box(
            Modifier
                .offset(x = thumbOffset)
                .size(16.dp)
                .wrapContentSize(unbounded = true)
                .size(18.dp)
                .clip(CircleShape)
                .background(trackColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onToggle(!enabled) },
                )
                .drawBehind {
                    drawCircle(
                        color = thumbCenter,
                        radius = (size.minDimension / 2) - 2.dp.toPx(),
                    )
                }
        )
    }
}

// ── Settings menu item (mirrors KotlinConf PageMenuItem) ─────────────────────

@Composable
private fun SettingsMenuItem(
    title: String,
    onClick: () -> Unit,
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(colors.surfaceBackground)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = typography.h3,
            color = colors.primaryText,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(R.drawable.arrow_left_24),
            contentDescription = null,
            tint = colors.primaryText,
            modifier = Modifier
                .size(20.dp)
                .scale(scaleX = -1f, scaleY = 1f),
        )
    }
}
