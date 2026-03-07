package ru.hse.edu.geoar.location

import android.util.Log
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.hse.edu.geoar.math.Dimens
import ru.hse.edu.geoar.sensors.HeadingProvider
import kotlin.math.cos
import kotlin.math.sin

data class ArFrameData(
    val userLocation: LocationData,
    val userHeading: Float,
    val frame: Frame,
    val cameraPose: Pose,
    val initialCameraHeading: Float,
    val initialPose: Pose,
)

class ArPoseLocationTracker(
    private val headingProvider: HeadingProvider,
    private val sceneView: ARSceneView,
    private val locationTracker: LocationTracker,
    private val scope: CoroutineScope,
) {

    private var updateJob: Job? = null

    private var initialPose: Pose? = null
    private var initialHeading: Float? = null
    private var initialLocation: LocationData? = null
    private var cameraTracking = false
    private var lastHeading: Float? = null
    private var lastLocation: LocationData? = null

    var onFrameUpdate: ((ArFrameData) -> Unit)? = null

    fun start() {
        locationTracker.start()
        updateJob = scope.launch {
            combine(
                headingProvider.smoothedValue,
                locationTracker.locationState
            ) { heading, locationResult ->
                heading to locationResult
            }.collect { (heading, locationResult) ->
                lastHeading = heading
                lastLocation = locationResult.unwrapOrNull()
                Log.d("ArPoseLocationTracker", "initHeading: $initialHeading; lastHeading: $lastHeading;")
            }
        }
        sceneView.onSessionUpdated = { _, frame ->
            val camera = frame.camera
            val pose = camera.pose
            val isTracking = camera.trackingState == TrackingState.TRACKING

            when {
                isTracking && !cameraTracking -> {
                    cameraTracking = true
                    initialPose = pose
                    initialHeading = lastHeading
                    initialLocation = lastLocation
                }

                isTracking && cameraTracking -> {
                    if (initialHeading == null) initialHeading = lastHeading
                    if (initialLocation == null) initialLocation = lastLocation
                }

                !isTracking && cameraTracking -> {
                    cameraTracking = false
                    initialPose = null
                    initialHeading = null
                    initialLocation = null
                }
            }

            val initPose = initialPose
            val initHeading = initialHeading
            val initLocation = initialLocation

            if (cameraTracking && initPose != null && initHeading != null && initLocation != null) {
                val location = computeLocation(
                    pose = pose,
                    initialPose = initPose,
                    initialHeading = initHeading,
                    initialLocation = initLocation
                )
                onFrameUpdate?.invoke(
                    ArFrameData(
                        userLocation = location,
                        userHeading = lastHeading ?: initHeading,
                        frame = frame,
                        cameraPose = pose,
                        initialCameraHeading = initHeading,
                        initialPose = initPose,
                    )
                )
            }
        }
    }

    fun stop() {
        updateJob?.cancel()
        updateJob = null
        locationTracker.stop()
        sceneView.onSessionUpdated = null
        cameraTracking = false
        initialPose = null
        initialHeading = null
        initialLocation = null
        lastHeading = null
        lastLocation = null
    }

    private fun computeLocation(
        pose: Pose,
        initialPose: Pose,
        initialHeading: Float,
        initialLocation: LocationData,
    ): LocationData {
        val dx = (pose.tx() - initialPose.tx()).toDouble()
        val dz = (pose.tz() - initialPose.tz()).toDouble()
        val forward = -dz
        val right = dx
        val headingRad = Math.toRadians(initialHeading.toDouble())
        val sinH = sin(headingRad)
        val cosH = cos(headingRad)
        val eastMeters = forward * sinH + right * cosH
        val northMeters = forward * cosH - right * sinH
        val deltaLat = northMeters / Dimens.metersPerDegreeLatitude()
        val metersPerDegLon = Dimens.metersPerDegreeLongitude(initialLocation.latitude)
        val deltaLon = if (metersPerDegLon != 0.0) eastMeters / metersPerDegLon else 0.0
        return LocationData(
            latitude = initialLocation.latitude + deltaLat,
            longitude = initialLocation.longitude + deltaLon,
            accuracy = AR_ACCURACY_METERS,
            timestamp = System.currentTimeMillis(),
        )
    }

    companion object {
        private const val AR_ACCURACY_METERS = 0.5f
    }
}

