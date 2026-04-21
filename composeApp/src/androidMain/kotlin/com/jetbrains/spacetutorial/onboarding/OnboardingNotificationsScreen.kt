package com.jetbrains.spacetutorial.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import com.jetbrains.spacetutorial.ui.OnboardingButton
import com.jetbrains.spacetutorial.ui.OnboardingHeroIcon
import com.jetbrains.spacetutorial.ui.OnboardingToggleItem
import androidx.compose.material3.Text as M3Text

@Composable
fun OnboardingNotificationsScreen(
    viewModel: OnboardingViewModel,
    onDone: () -> Unit,
) {
    val prefs by viewModel.notificationPrefs.collectAsStateWithLifecycle()
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography

    // ── Notification permission launcher (Android 13+) ────────────────────────
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setNotificationPrefs(prefs.copy(sessionReminders = granted))
    }

    // ── Location permission launcher ──────────────────────────────────────────
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setNotificationPrefs(prefs.copy(appUpdates = granted))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
        ) {
            OnboardingHeroIcon()

            M3Text(
                text = "Stay Informed",
                style = typography.h1,
                color = colors.primaryText,
                modifier = Modifier.semantics { heading() },
            )

            M3Text(
                text = "Grant permissions so we can alert you about offenders nearby and keep you up to date.",
                style = typography.text1,
                color = colors.secondaryText,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Nearby Alerts → asks notification permission
                OnboardingToggleItem(
                    title = "Nearby Alerts",
                    description = "Get notified when a new offender is registered near you.",
                    enabled = prefs.sessionReminders,
                    onToggle = { checked ->
                        if (checked) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                // Pre-Android 13: notifications on by default, no runtime permission needed
                                viewModel.setNotificationPrefs(prefs.copy(sessionReminders = true))
                            }
                        } else {
                            viewModel.setNotificationPrefs(prefs.copy(sessionReminders = false))
                        }
                    },
                )
                // Location Alerts → asks location permission
                OnboardingToggleItem(
                    title = "Location Alerts",
                    description = "Allow location access to find offenders near you.",
                    enabled = prefs.appUpdates,
                    onToggle = { checked ->
                        if (checked) {
                            locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        } else {
                            viewModel.setNotificationPrefs(prefs.copy(appUpdates = false))
                        }
                    },
                )
            }
        }

        HorizontalDivider(color = colors.strokePale, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            OnboardingButton(
                label = "Let's get started",
                onClick = {
                    viewModel.completeOnboarding()
                    onDone()
                },
                primary = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
