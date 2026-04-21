package com.jetbrains.spacetutorial

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderSummary
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import com.jetbrains.spacetutorial.texaswatch.ui.OffenderCard
import com.jetbrains.spacetutorial.R
import com.jetbrains.spacetutorial.ui.MainHeaderTitleBar
import com.jetbrains.spacetutorial.ui.TopMenuButton
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.koin.androidx.compose.koinViewModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffendersSnapAndSearchScreen(onSearchClick: () -> Unit = {}) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    val context = LocalContext.current
    val viewModel: NearbyOffendersViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> viewModel.onPermissionResult(granted) }

    val alreadyGranted = remember {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    LaunchedEffect(Unit) { viewModel.checkPermissionAndLoad(alreadyGranted) }

    // Trigger next page when within 5 items of the bottom
    LaunchedEffect(listState) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            lastVisible >= total - 5
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { viewModel.loadNextPage() }
    }

    val coroutineScope = rememberCoroutineScope()
    val showBackToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 3 } }

    PullToRefreshBox(
        isRefreshing = state.isLoading && state.offenders.isNotEmpty(),
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(colors.mainBackground),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        // ── Header bar ────────────────────────────────────────────────────────
        item {
            MainHeaderTitleBar(
                title = "Offenders",
                endContent = {
                    TopMenuButton(
                        iconRes = R.drawable.search_28,
                        contentDescription = "Search",
                        onClick = onSearchClick,
                    )
                },
            )
            HorizontalDivider(thickness = 1.dp, color = colors.strokePale)
        }

        // ── Nearby Offenders Card ─────────────────────────────────────────────
        item {
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
        }

        // ── Radius Slider ─────────────────────────────────────────────────────
        if (state.locationGranted) {
            item {
                Text(
                    text = "${"%.1f".format(state.radiusMiles)} mi radius",
                    style = typography.text2,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 2.dp),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            }

            // ── Section header ────────────────────────────────────────────────
            item {
                Text(
                    text = "Nearest Offenders",
                    style = typography.h4,
                    color = colors.primaryText,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                )
            }

            // ── Offender list — shimmer while loading, real cards when ready ──
            if (state.isListLoading) {
                items(8) {
                    ShimmerOffenderCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
            } else {
                val userLat = state.userLat
                val userLon = state.userLon
                itemsIndexed(state.offenders, key = { _, o -> o.indIdn }) { _, offender ->
                    OffenderCard(
                        offender = offender,
                        distanceMiles = if (userLat != null && userLon != null)
                            haversine(userLat, userLon, offender) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }

                // ── Load-more footer ─────────────────────────────────────────
                if (state.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = colors.ringActive,
                                strokeWidth = 3.dp,
                            )
                        }
                    }
                }
            }
        }
    } // LazyColumn

    AnimatedVisibility(
        visible = showBackToTop,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .navigationBarsPadding()
            .padding(16.dp),
    ) {
        FloatingActionButton(
            onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } },
            shape = CircleShape,
            containerColor = colors.ringActive,
            contentColor = colors.invertedText,
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "Back to top",
                modifier = Modifier.size(24.dp),
            )
        }
    }
    } // PullToRefreshBox
}

private fun haversine(userLat: Double, userLon: Double, offender: OffenderSummary): Double? {
    val lat2 = offender.lat ?: return null
    val lon2 = offender.lon ?: return null
    val r = 3958.8
    val dLat = Math.toRadians(lat2 - userLat)
    val dLon = Math.toRadians(lon2 - userLon)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(userLat)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}
