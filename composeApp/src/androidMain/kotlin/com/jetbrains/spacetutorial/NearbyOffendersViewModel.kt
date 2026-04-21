package com.jetbrains.spacetutorial

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.jetbrains.spacetutorial.texaswatch.TexasWatchSDK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "NearbyVM"

data class NearbyOffendersState(
    val isLoading: Boolean = false,
    val count: Int = 0,
    val radiusMiles: Float = 5f,
    val locationGranted: Boolean = false,
    val error: String? = null,
)

class NearbyOffendersViewModel(
    application: Application,
    private val sdk: TexasWatchSDK,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(NearbyOffendersState())
    val state: StateFlow<NearbyOffendersState> = _state.asStateFlow()

    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)

    private val cache = mutableMapOf<String, Pair<Int, Long>>()
    private val cacheTtlMs = 10 * 60 * 1000L

    fun onPermissionResult(granted: Boolean) {
        Log.d(TAG, "onPermissionResult: granted=$granted")
        _state.value = _state.value.copy(locationGranted = granted)
        if (granted) fetchNearby(_state.value.radiusMiles)
    }

    // Called on every drag tick — just update label, no API call
    fun onRadiusChange(miles: Float) {
        _state.value = _state.value.copy(radiusMiles = miles)
    }

    // Called when finger lifts — fire API
    fun onRadiusChangeFinished() {
        val miles = _state.value.radiusMiles
        Log.d(TAG, "onRadiusChangeFinished: $miles mi")
        if (_state.value.locationGranted) fetchNearby(miles)
    }

    fun checkPermissionAndLoad(granted: Boolean) {
        Log.d(TAG, "checkPermissionAndLoad: granted=$granted")
        _state.value = _state.value.copy(locationGranted = granted)
        if (granted) fetchNearby(_state.value.radiusMiles)
    }

    @SuppressLint("MissingPermission")
    private fun fetchNearby(radiusMiles: Float) {
        viewModelScope.launch {
            Log.d(TAG, "fetchNearby: starting, radius=$radiusMiles")
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                // getLastLocation is instant on emulator and real devices after first fix
                var location: Location? = fusedClient.lastLocation.await()

                // If no last location, do a fresh request with short timeout
                if (location == null) {
                    Log.w(TAG, "fetchNearby: lastLocation null, trying getCurrentLocation")
                    location = fusedClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token,
                    ).await()
                }

                if (location == null) {
                    Log.e(TAG, "fetchNearby: no location available")
                    _state.value = _state.value.copy(isLoading = false, error = "Could not get location")
                    return@launch
                }

                val lat = location.latitude
                val lon = location.longitude
                Log.d(TAG, "fetchNearby: got location lat=$lat lon=$lon")

                val key = "%.3f,%.3f,%.1f".format(lat, lon, radiusMiles)
                val cached = cache[key]
                if (cached != null && System.currentTimeMillis() - cached.second < cacheTtlMs) {
                    Log.d(TAG, "fetchNearby: cache hit, count=${cached.first}")
                    _state.value = _state.value.copy(isLoading = false, count = cached.first)
                    return@launch
                }

                Log.d(TAG, "fetchNearby: calling API lat=$lat lon=$lon radius=$radiusMiles")
                val stats = sdk.getRiskStats(lat, lon, radiusMiles.toDouble())
                val total = stats.lowAndModerateCount + stats.highRiskCount
                Log.d(TAG, "fetchNearby: API response low=${stats.lowAndModerateCount} high=${stats.highRiskCount} total=$total")
                cache[key] = Pair(total, System.currentTimeMillis())
                _state.value = _state.value.copy(isLoading = false, count = total)
            } catch (e: Exception) {
                Log.e(TAG, "fetchNearby: ERROR ${e::class.simpleName}: ${e.message}", e)
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }
}
