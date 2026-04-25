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
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.jetbrains.spacetutorial.texaswatch.TexasWatchSDK
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import com.jetbrains.spacetutorial.ui.MainHeaderTitleBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// ── Route step state ──────────────────────────────────────────────────────────

sealed class RouteStep {
    object Idle : RouteStep()
    object PickingEnd : RouteStep()
    object Calculating : RouteStep()
    object LoadingPins : RouteStep()
    data class Ready(val pinCount: Int) : RouteStep()
    data class Error(val msg: String) : RouteStep()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class RouteScreenViewModel : ViewModel(), KoinComponent {
    private val sdk: TexasWatchSDK by inject()

    var step by mutableStateOf<RouteStep>(RouteStep.Idle)
    var startLatLng by mutableStateOf<LatLng?>(null)
    var endLatLng by mutableStateOf<LatLng?>(null)
    var routePoints by mutableStateOf<List<LatLng>>(emptyList())
    var offenderPins by mutableStateOf<List<MapOffenderPin>>(emptyList())

    fun onMapTap(latLng: LatLng) {
        when (step) {
            is RouteStep.Idle -> {
                startLatLng = latLng
                endLatLng = null
                routePoints = emptyList()
                offenderPins = emptyList()
                step = RouteStep.PickingEnd
            }
            is RouteStep.PickingEnd -> {
                endLatLng = latLng
                startRoute()
            }
            else -> {
                // Reset
                startLatLng = latLng
                endLatLng = null
                routePoints = emptyList()
                offenderPins = emptyList()
                step = RouteStep.PickingEnd
            }
        }
    }

    fun rePickStart() { step = RouteStep.Idle }
    fun rePickEnd() {
        if (startLatLng != null) {
            endLatLng = null
            routePoints = emptyList()
            offenderPins = emptyList()
            step = RouteStep.PickingEnd
        }
    }

    fun clearRoute() {
        startLatLng = null
        endLatLng = null
        routePoints = emptyList()
        offenderPins = emptyList()
        step = RouteStep.Idle
    }

    private fun startRoute() {
        val start = startLatLng ?: return
        val end = endLatLng ?: return
        viewModelScope.launch {
            step = RouteStep.Calculating
            try {
                val points = withContext(Dispatchers.IO) {
                    kotlinx.coroutines.withTimeout(15_000) { fetchRoutePoints(start, end) }
                }
                if (points.isEmpty()) { step = RouteStep.Error("No route found"); return@launch }
                routePoints = points
                step = RouteStep.LoadingPins
                val pins = loadOffendersAlongRoute(points)
                offenderPins = pins
                step = RouteStep.Ready(pins.size)
            } catch (e: kotlinx.coroutines.CancellationException) {
                step = RouteStep.Idle
            } catch (e: Exception) {
                step = RouteStep.Error(e.message ?: "Error")
            }
        }
    }

    private suspend fun fetchRoutePoints(start: LatLng, end: LatLng): List<LatLng> {
        val apiKey = "AIzaSyDioo6Q-5ZUa_NfJ7z6v9njmv7tIJyrwn"
        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${start.latitude},${start.longitude}" +
                "&destination=${end.latitude},${end.longitude}" +
                "&mode=driving&key=$apiKey"
        val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
        conn.connectTimeout = 10_000
        val json = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
        return decodeDirectionsResponse(json)
    }

    private fun decodeDirectionsResponse(json: String): List<LatLng> {
        // Extract overview_polyline.points
        val marker = "\"overview_polyline\""
        val idx = json.indexOf(marker).takeIf { it >= 0 } ?: return emptyList()
        val pointsStart = json.indexOf("\"points\"", idx)
        if (pointsStart < 0) return emptyList()
        val colonIdx = json.indexOf(":", pointsStart)
        val quoteOpen = json.indexOf("\"", colonIdx + 1)
        val quoteClose = json.indexOf("\"", quoteOpen + 1)
        val encoded = json.substring(quoteOpen + 1, quoteClose)
        return decodePolyline(encoded)
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val result = mutableListOf<LatLng>()
        var index = 0; var lat = 0; var lng = 0
        while (index < encoded.length) {
            var b: Int; var shift = 0; var result2 = 0
            do { b = encoded[index++].code - 63; result2 = result2 or ((b and 0x1f) shl shift); shift += 5 } while (b >= 0x20)
            lat += if (result2 and 1 != 0) (result2 shr 1).inv() else result2 shr 1
            shift = 0; result2 = 0
            do { b = encoded[index++].code - 63; result2 = result2 or ((b and 0x1f) shl shift); shift += 5 } while (b >= 0x20)
            lng += if (result2 and 1 != 0) (result2 shr 1).inv() else result2 shr 1
            result.add(LatLng(lat / 1e5, lng / 1e5))
        }
        return result
    }

    private suspend fun loadOffendersAlongRoute(points: List<LatLng>): List<MapOffenderPin> {
        val intervalMetres = 800.0  // ~0.5 miles
        val samplePoints = mutableListOf<LatLng>()
        samplePoints.add(points.first())
        var accumulated = 0.0
        for (i in 1 until points.size) {
            val prev = points[i - 1]; val curr = points[i]
            val dlat = curr.latitude - prev.latitude; val dlon = curr.longitude - prev.longitude
            accumulated += Math.sqrt(dlat * dlat + dlon * dlon) * 111_000
            if (accumulated >= intervalMetres) { samplePoints.add(curr); accumulated = 0.0 }
        }
        if (samplePoints.last() != points.last()) samplePoints.add(points.last())

        val seen = mutableSetOf<Int>()
        val all = mutableListOf<MapOffenderPin>()
        samplePoints.chunked(8).forEach { batch ->
            batch.forEach { coord ->
                try {
                    val resp = sdk.getOffendersByRadiusForMap(coord.latitude, coord.longitude, 0.5, 0, 50)
                    resp.content.forEach { o ->
                        val lat2 = o.latitude?.toDouble() ?: return@forEach
                        val lon2 = o.longitude?.toDouble() ?: return@forEach
                        val id = o.indIdn.toInt()
                        if (seen.add(id)) {
                            all.add(MapOffenderPin(id, lat2, lon2, o.fullName, o.photoUrl))
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        return all
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(
    onOffenderDetail: (Int) -> Unit = {},
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    val vm: RouteScreenViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
        if (granted) locationGranted = true
        else locationLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ))
    }

    LaunchedEffect(locationGranted) {
        if (!locationGranted) return@LaunchedEffect
        try {
            LocationServices.getFusedLocationProviderClient(context).lastLocation
                .addOnSuccessListener { loc ->
                    if (loc != null) scope.launch {
                        cameraState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 13f))
                    }
                }
        } catch (_: SecurityException) {}
    }

    // Fit camera to route when ready
    LaunchedEffect(vm.routePoints) {
        if (vm.routePoints.size >= 2) {
            val builder = com.google.android.gms.maps.model.LatLngBounds.Builder()
            vm.routePoints.forEach { builder.include(it) }
            val bounds = builder.build()
            cameraState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 120))
        }
    }

    val accentArgb = android.graphics.Color.parseColor("#1A3A5C") // navy approximation

    Column(Modifier.fillMaxSize().background(colors.mainBackground)) {
        MainHeaderTitleBar(title = "Route")

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
                onMapClick = { latLng -> vm.onMapTap(latLng) },
            ) {
                // Route polyline
                if (vm.routePoints.size >= 2) {
                    Polyline(
                        points = vm.routePoints,
                        color = colors.primaryAccent,
                        width = 12f,
                    )
                }

                // Start marker A
                vm.startLatLng?.let { start ->
                    Marker(
                        state = MarkerState(position = start),
                        title = "Start",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                    )
                }

                // End marker B
                vm.endLatLng?.let { end ->
                    Marker(
                        state = MarkerState(position = end),
                        title = "End",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                    )
                }

                // Offender pins
                vm.offenderPins.forEach { pin ->
                    Marker(
                        state = MarkerState(position = LatLng(pin.lat, pin.lon)),
                        title = pin.fullName,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                        onClick = { selectedPin = pin; true }
                    )
                }
            }

            // ── Instruction banner ────────────────────────────────────────────
            val bannerText = when (vm.step) {
                is RouteStep.Idle, is RouteStep.PickingEnd -> "Pick a start and end point to search offenders along a route"
                is RouteStep.Ready, is RouteStep.Error -> "Tap map to start over"
                else -> null
            }
            bannerText?.let {
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = colors.mainBackground.copy(alpha = 0.95f),
                    shadowElevation = 4.dp,
                ) {
                    Text(it, style = typography.text2.copy(fontWeight = FontWeight.Medium),
                        color = colors.primaryText,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp))
                }
            }

            // ── Status pill ───────────────────────────────────────────────────
            val statusText = when (val s = vm.step) {
                is RouteStep.Calculating -> "Calculating route…"
                is RouteStep.LoadingPins -> "Loading offenders along route…"
                is RouteStep.Ready -> if (s.pinCount == 0) "No offenders along this route" else "${s.pinCount} offenders along this route"
                is RouteStep.Error -> s.msg
                else -> null
            }
            val statusLoading = vm.step is RouteStep.Calculating || vm.step is RouteStep.LoadingPins
            statusText?.let {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = colors.primaryAccent.copy(alpha = 0.92f),
                    shadowElevation = 4.dp,
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (statusLoading) {
                            CircularProgressIndicator(Modifier.size(14.dp), color = colors.invertedText, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(it, style = typography.text2.copy(fontWeight = FontWeight.Medium), color = colors.invertedText)
                    }
                }
            }

            // ── Pin control bar ───────────────────────────────────────────────
            if (vm.startLatLng != null || vm.endLatLng != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    // Clear button
                    Surface(
                        modifier = Modifier.clickable { vm.clearRoute() },
                        shape = RoundedCornerShape(12.dp),
                        color = colors.mainBackground,
                        shadowElevation = 3.dp,
                    ) {
                        Column(
                            Modifier.padding(horizontal = 32.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("✕", style = typography.h4, color = colors.dangerText)
                            Spacer(Modifier.height(3.dp))
                            Text("Clear", style = typography.label, color = colors.dangerText)
                        }
                    }
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
