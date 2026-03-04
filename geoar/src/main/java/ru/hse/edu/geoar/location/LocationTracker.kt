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
import ru.hse.edu.geoar.sensors.HeadingProvider
import ru.hse.edu.geoar.sensors.LinearAccelerationProvider
import ru.hse.edu.geoar.sensors.SensorsManager
import ru.hse.edu.geoar.sensors.StepDetectorProvider
import ru.hse.locallense.common.ResultContainer

@SuppressLint("MissingPermission")
class LocationTracker(
    heading: HeadingProvider,
    step: StepDetectorProvider,
    acceleration: LinearAccelerationProvider,
    private val scope: CoroutineScope,
    context: Context
) {

    private val appContext = context.applicationContext
    private val fusedClient = LocationServices.getFusedLocationProviderClient(appContext)

    private val kalman = LocationKalmanFilter()
    private val sensorsManager = SensorsManager(heading, step, acceleration)

    private val _locationState =
        MutableStateFlow<ResultContainer<LocationData>>(ResultContainer.Loading)
    val locationState: StateFlow<ResultContainer<LocationData>> = _locationState.asStateFlow()

    private var callback: LocationCallback? = null

    fun start() {
        if (callback != null) return

        _locationState.value = ResultContainer.Loading
        kalman.reset()

        if (!LocationPermissionHelper.hasPermission(appContext)) {
            _locationState.value = ResultContainer.Error(PermissionDeniedException())
            return
        }
        if (!LocationPermissionHelper.isGpsEnabled(appContext)) {
            _locationState.value = ResultContainer.Error(GpsDisabledException())
            return
        }

        sensorsManager.start(
            scope = scope,
            onStep = { azimuth ->
                kalman.predictStep(sensorsManager.stepLengthMeters, azimuth)
                emitCurrentEstimate()
            },
            onMovementChanged = { moving ->
                kalman.setMoving(moving)
            }
        )

        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { raw ->
                    val measurement = raw.toLocationData()
                    val filtered = kalman.process(measurement)
                    if (filtered != null) {
                        _locationState.value = ResultContainer.Done(filtered)
                    }
                }
            }
        }

        try {
            fusedClient.requestLocationUpdates(
                buildRequest(), callback!!, Looper.getMainLooper()
            )
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
        sensorsManager.stop()
    }

    private fun emitCurrentEstimate() {
        val estimate = kalman.buildEstimateNow() ?: return
        _locationState.value = ResultContainer.Done(estimate)
    }

    private fun buildRequest() =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2_000L)
            .setMinUpdateIntervalMillis(1_000L)
            .setMinUpdateDistanceMeters(0f)
            .setWaitForAccurateLocation(true)
            .build()

    private fun Location.toLocationData() = LocationData(
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        timestamp = time
    )
}