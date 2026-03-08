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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.hse.edu.geoar.sensors.SensorsManager
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.common.entities.LocationData

@SuppressLint("MissingPermission")
class LocationTracker(
    private val sensorsManager: SensorsManager,
    private val scope: CoroutineScope,
    context: Context
) {
    private val applicationContext = context.applicationContext
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(applicationContext)

    private val locationKalmanFilter = LocationKalmanFilter()

    private val _locationState =
        MutableStateFlow<LocationData?>(null)
    val locationState: StateFlow<LocationData?> = _locationState.asStateFlow()

    private var locationCallback: LocationCallback? = null

    fun start() {
        if (locationCallback != null) return
        locationKalmanFilter.reset()

        sensorsManager.start(
            scope = scope,
            onStep = { azimuth ->
                locationKalmanFilter.predictStep(azimuth)
                emitCurrentEstimate()
            },
            onMovementChanged = { isMoving ->
                locationKalmanFilter.setMoving(isMoving)
            }
        )

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { rawLocation ->
                    val measurement = rawLocation.toLocationFix()
                    val filteredLocation = locationKalmanFilter.process(measurement)
                    if (filteredLocation != null) {
                        _locationState.value = filteredLocation
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            buildLocationRequest(), locationCallback!!, Looper.getMainLooper()
        )
    }

    fun stop() {
        locationCallback?.let { callback ->
            fusedLocationClient.removeLocationUpdates(callback)
            locationCallback = null
        }
        sensorsManager.stop()
    }

    private fun emitCurrentEstimate() {
        val estimate = locationKalmanFilter.buildEstimateNow() ?: return
        _locationState.value = estimate
    }

    private fun buildLocationRequest() =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2_000L)
            .setMinUpdateIntervalMillis(1_000L)
            .setMinUpdateDistanceMeters(0f)
            .setWaitForAccurateLocation(true)
            .build()
}