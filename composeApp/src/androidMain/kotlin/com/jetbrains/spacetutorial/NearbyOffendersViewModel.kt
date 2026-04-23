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
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "NearbyVM"
private const val PAGE_SIZE = 20

data class NearbyOffendersState(
    val isLoading: Boolean = false,
    val isListLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val count: Int = 0,
    val offenders: List<OffenderSummary> = emptyList(),
    val radiusMiles: Float = 5.0f,
    val locationGranted: Boolean = false,
    val userLat: Double? = null,
    val userLon: Double? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val error: String? = null,
)

class NearbyOffendersViewModel(
    application: Application,
    private val sdk: TexasWatchSDK,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(NearbyOffendersState())
    val state: StateFlow<NearbyOffendersState> = _state.asStateFlow()

    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)

    fun onPermissionResult(granted: Boolean) {
        Log.d(TAG, "onPermissionResult: granted=$granted")
        _state.value = _state.value.copy(locationGranted = granted)
        if (granted) fetchPage(page = 0, resetList = true)
    }

    fun onRadiusChange(miles: Float) {
        _state.value = _state.value.copy(radiusMiles = miles)
    }

    fun onRadiusChangeFinished() {
        val miles = _state.value.radiusMiles
        Log.d(TAG, "onRadiusChangeFinished: $miles mi")
        if (_state.value.locationGranted) {
            _state.value = _state.value.copy(isListLoading = true, offenders = emptyList())
            fetchPage(page = 0, resetList = true)
        }
    }

    fun refresh() {
        Log.d(TAG, "refresh: force reload page 0")
        if (_state.value.locationGranted) fetchPage(page = 0, resetList = true)
    }

    fun checkPermissionAndLoad(granted: Boolean) {
        Log.d(TAG, "checkPermissionAndLoad: granted=$granted")
        _state.value = _state.value.copy(locationGranted = granted)
        if (granted) fetchPage(page = 0, resetList = true)
    }

    fun loadNextPage() {
        val s = _state.value
        if (s.isLoadingMore || s.isListLoading || s.currentPage + 1 >= s.totalPages) return
        Log.d(TAG, "loadNextPage: page=${s.currentPage + 1}")
        fetchPage(page = s.currentPage + 1, resetList = false)
    }

    @SuppressLint("MissingPermission")
    private fun fetchPage(page: Int, resetList: Boolean) {
        viewModelScope.launch {
            val isFirstPage = page == 0
            Log.d(TAG, "fetchPage: page=$page resetList=$resetList")

            if (isFirstPage) {
                _state.value = _state.value.copy(isLoading = true, isListLoading = true, error = null)
            } else {
                _state.value = _state.value.copy(isLoadingMore = true)
            }

            try {
                val location = resolveLocation() ?: run {
                    _state.value = _state.value.copy(isLoading = false, isListLoading = false, isLoadingMore = false, error = "Could not get location")
                    return@launch
                }

                val lat = location.latitude
                val lon = location.longitude
                val radiusMiles = _state.value.radiusMiles.toDouble()

                Log.d(TAG, "fetchPage: lat=$lat lon=$lon radius=$radiusMiles page=$page")
                val result = sdk.getOffendersPage(lat, lon, radiusMiles, page = page, size = PAGE_SIZE)
                Log.d(TAG, "fetchPage: got ${result.offenders.size} offenders, total=${result.totalCount}, totalPages=${result.totalPages}")

                val existing = if (resetList) emptyList() else _state.value.offenders
                _state.value = _state.value.copy(
                    isLoading = false,
                    isListLoading = false,
                    isLoadingMore = false,
                    count = result.totalCount,
                    offenders = existing + result.offenders,
                    userLat = lat,
                    userLon = lon,
                    currentPage = page,
                    totalPages = result.totalPages,
                )
            } catch (e: Exception) {
                Log.e(TAG, "fetchPage: ERROR ${e::class.simpleName}: ${e.message}", e)
                _state.value = _state.value.copy(isLoading = false, isListLoading = false, isLoadingMore = false, error = e.message ?: "Unknown error")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun resolveLocation(): Location? {
        // Always request a fresh location — lastLocation can be stale (e.g. cached emulator coords)
        Log.d(TAG, "resolveLocation: requesting fresh getCurrentLocation")
        var location: Location? = fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token).await()
        if (location == null) {
            Log.w(TAG, "resolveLocation: getCurrentLocation null, falling back to lastLocation")
            location = fusedClient.lastLocation.await()
        }
        if (location != null) {
            Log.d(TAG, "resolveLocation: lat=${location.latitude} lon=${location.longitude} accuracy=${location.accuracy}m provider=${location.provider}")
        } else {
            Log.e(TAG, "resolveLocation: FAILED — both getCurrentLocation and lastLocation returned null. Check emulator location settings.")
        }
        return location
    }
}
