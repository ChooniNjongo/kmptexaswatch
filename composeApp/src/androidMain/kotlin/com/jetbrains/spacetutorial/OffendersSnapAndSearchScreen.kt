package com.jetbrains.spacetutorial

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import com.jetbrains.spacetutorial.ui.MainHeaderTitleBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun OffendersSnapAndSearchScreen() {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    val context = LocalContext.current
    val viewModel: NearbyOffendersViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    // Check current permission on first composition
    val alreadyGranted = remember {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    LaunchedEffect(Unit) {
        viewModel.checkPermissionAndLoad(alreadyGranted)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .verticalScroll(rememberScrollState())
    ) {
        MainHeaderTitleBar(title = "Offenders")

        HorizontalDivider(thickness = 1.dp, color = colors.strokePale)

        // ── Nearby Offenders Card ─────────────────────────────────────────────
        NearbyOffendersCard(
            count = state.count,
            progress = if (state.isLoading) 0f else if (state.locationGranted) 1f else 0f,
            locationActive = state.locationGranted,
            isLoading = state.isLoading,
            onAllowLocation = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
            onOpenSettings = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                )
            },
        )

        // ── Radius Slider ─────────────────────────────────────────────────────
        if (state.locationGranted) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "${"%.1f".format(state.radiusMiles)} mi radius",
                    style = typography.text2,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                Slider(
                    value = state.radiusMiles,
                    onValueChange = { viewModel.onRadiusChange(it) },
                    onValueChangeFinished = { viewModel.onRadiusChangeFinished() },
                    valueRange = 0.5f..5f,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.ringActive,
                        activeTrackColor = colors.ringActive,
                        inactiveTrackColor = colors.ringTrack,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
