package com.jetbrains.spacetutorial

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import com.jetbrains.spacetutorial.ui.MainHeaderTitleBar
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// ── Data model ────────────────────────────────────────────────────────────────

data class MapOffenderPin(
    val id: Int,
    val lat: Double,
    val lon: Double,
    val fullName: String,
    val photoUrl: String?,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class MapScreenViewModel : ViewModel(), KoinComponent {
    private val sdk: com.jetbrains.spacetutorial.texaswatch.TexasWatchSDK by inject()

    var pins by mutableStateOf<List<MapOffenderPin>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set

    private var lastLat = Double.NaN
    private var lastLon = Double.NaN

    fun loadPins(lat: Double, lon: Double) {
        // Skip if moved < ~800m
        if (!lastLat.isNaN()) {
            val dlat = lat - lastLat; val dlon = lon - lastLon
            if (dlat * dlat + dlon * dlon < 0.0001) return
        }
        lastLat = lat; lastLon = lon
        viewModelScope.launch {
            isLoading = true
            try {
                val resp = sdk.getOffendersByRadiusForMap(lat, lon, 5.0, 0, 50)
                pins = resp.content.mapNotNull { o ->
                    val lat2 = o.latitude?.toDouble() ?: return@mapNotNull null
                    val lon2 = o.longitude?.toDouble() ?: return@mapNotNull null
                    MapOffenderPin(
                        id = o.indIdn.toInt(),
                        lat = lat2, lon = lon2,
                        fullName = o.fullName,
                        photoUrl = o.photoUrl,
                    )
                }
            } catch (_: Exception) {}
            isLoading = false
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onOffenderDetail: (Int) -> Unit = {},
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    val vm: MapScreenViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Texas center default
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(31.0, -100.0), 6f)
    }

    var locationGranted by remember { mutableStateOf(false) }
    var selectedPin by remember { mutableStateOf<MapOffenderPin?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        locationGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (granted) {
            locationGranted = true
        } else {
            locationLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ))
        }
    }

    // Zoom to user location once granted
    LaunchedEffect(locationGranted) {
        if (!locationGranted) return@LaunchedEffect
        try {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            fused.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    scope.launch {
                        cameraState.animate(
                            CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 13f)
                        )
                        vm.loadPins(loc.latitude, loc.longitude)
                    }
                }
            }
        } catch (_: SecurityException) {}
    }

    Column(Modifier.fillMaxSize().background(colors.mainBackground)) {
        MainHeaderTitleBar(title = "Map")

        Box(Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                properties = MapProperties(isMyLocationEnabled = locationGranted),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = true,
                    compassEnabled = true,
                ),
                onMapLoaded = {
                    val pos = cameraState.position.target
                    vm.loadPins(pos.latitude, pos.longitude)
                },
            ) {
                vm.pins.forEach { pin ->
                    Marker(
                        state = MarkerState(position = LatLng(pin.lat, pin.lon)),
                        title = pin.fullName,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                        onClick = {
                            selectedPin = pin
                            true
                        }
                    )
                }
            }

            // Reload on camera idle
            LaunchedEffect(cameraState.isMoving) {
                if (!cameraState.isMoving) {
                    val pos = cameraState.position.target
                    vm.loadPins(pos.latitude, pos.longitude)
                }
            }

            // Loading badge
            if (vm.isLoading) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 80.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = colors.mainBackground.copy(alpha = 0.92f),
                    shadowElevation = 4.dp,
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = colors.primaryAccent)
                        Spacer(Modifier.width(8.dp))
                        Text("Loading…", style = typography.label, color = colors.secondaryText)
                    }
                }
            } else if (vm.pins.isNotEmpty()) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 80.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = colors.primaryAccent.copy(alpha = 0.92f),
                    shadowElevation = 4.dp,
                ) {
                    Text(
                        "${vm.pins.size} offenders nearby",
                        style = typography.label.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.invertedText,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }

    // ── Bottom sheet ──────────────────────────────────────────────────────────
    selectedPin?.let { pin ->
        ModalBottomSheet(
            onDismissRequest = { selectedPin = null },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = colors.mainBackground,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOffenderDetail(pin.id); selectedPin = null }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Photo or initials
                Box(
                    Modifier.size(52.dp).clip(CircleShape).background(colors.primaryAccent),
                    contentAlignment = Alignment.Center,
                ) {
                    if (pin.photoUrl != null) {
                        AsyncImage(
                            model = pin.photoUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                        )
                    } else {
                        val initials = pin.fullName.split(" ")
                            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                            .take(2).joinToString("")
                        Text(initials.ifEmpty { "?" }, style = typography.h4, color = colors.invertedText)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        pin.fullName.split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } },
                        style = typography.h4,
                        color = colors.primaryText,
                    )
                    Text("Tap to view full profile", style = typography.text2, color = colors.secondaryText)
                }
                Text("›", style = typography.h3, color = colors.secondaryText)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
