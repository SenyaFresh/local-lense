package ru.hse.edu.geoar.location

import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import ru.hse.locallense.common.ResultContainer

class LocationTracker(
    scope: CoroutineScope,
    context: Context,
) {
    private val appContext = context.applicationContext
    private val fusedClient = LocationServices.getFusedLocationProviderClient(appContext)

    private var isTracking = false

    fun isActive(): Boolean = isTracking

    val locationState: StateFlow<ResultContainer<LocationData>> = callbackFlow {
        trySend(ResultContainer.Loading)

        if (!LocationPermissionHelper.hasPermission(appContext)) {
            trySend(ResultContainer.Error(PermissionDeniedException()))
            close()
            return@callbackFlow
        }

        if (!LocationPermissionHelper.isGpsEnabled(appContext)) {
            trySend(ResultContainer.Error(GpsDisabledException()))
            close()
            return@callbackFlow
        }

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation
                if (loc != null) {
                    trySend(ResultContainer.Done(loc.toLocationData()))
                }
            }
        }

        try {
            fusedClient.requestLocationUpdates(buildRequest(), callback, Looper.getMainLooper())
        } catch (e: Exception) {
            trySend(ResultContainer.Error(UnknownLocationException(e)))
            close()
        }

        awaitClose {
            fusedClient.removeLocationUpdates(callback)
        }
    }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = ResultContainer.Loading
        )

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