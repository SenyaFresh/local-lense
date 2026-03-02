package ru.hse.edu.geoar.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.hse.locallense.common.ResultContainer

@SuppressLint("MissingPermission")
class LocationTracker(context: Context) {

    private val appContext = context.applicationContext
    private val fusedClient = LocationServices.getFusedLocationProviderClient(appContext)

    private val _locationState = MutableStateFlow<ResultContainer<LocationData>>(ResultContainer.Loading)
    val locationState: StateFlow<ResultContainer<LocationData>> = _locationState.asStateFlow()

    private var callback: LocationCallback? = null

    val isActive: Boolean
        get() = callback != null

    fun start() {
        if (callback != null) return

        _locationState.value = ResultContainer.Loading

        if (!LocationPermissionHelper.hasPermission(appContext)) {
            _locationState.value = ResultContainer.Error(PermissionDeniedException())
            return
        }

        if (!LocationPermissionHelper.isGpsEnabled(appContext)) {
            _locationState.value = ResultContainer.Error(GpsDisabledException())
            return
        }

        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    _locationState.value = ResultContainer.Done(it.toLocationData())
                }
            }
        }

        try {
            fusedClient.requestLocationUpdates(buildRequest(), callback!!, Looper.getMainLooper())
        } catch (e: Exception) {
            _locationState.value = ResultContainer.Error(UnknownLocationException(e))
            callback = null
        }
    }

    fun stop() {
        callback?.let {
            fusedClient.removeLocationUpdates(it)
            callback = null
        }
    }

    private fun buildRequest() = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1_000L)
        .setMinUpdateIntervalMillis(1_000L)
        .setMinUpdateDistanceMeters(0f)
        .setWaitForAccurateLocation(true)
        .build()

    private fun Location.toLocationData() = LocationData(
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        speed = speed,
        bearing = bearing,
        altitude = altitude,
        timestamp = time
    )
}