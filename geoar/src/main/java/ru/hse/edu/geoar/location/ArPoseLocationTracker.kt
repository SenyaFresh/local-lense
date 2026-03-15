package ru.hse.edu.geoar.location

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
import ru.hse.locallense.common.entities.LocationData
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class ArFrameData(
    val userLocation: LocationData,
    val userHeading: Float,
    val frame: Frame,
    val cameraPose: Pose,
    val initialCameraHeading: Float,
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
    private var initialYaw: Float = 0f
    private var lastHeading: Float? = null
    private var lastLocation: LocationData? = null
    private var lastPose: Pose? = null
    private var headingLocked = false

    val isHeadingLocked: Boolean
        get() = headingLocked

    var onFrameUpdate: ((ArFrameData) -> Unit)? = null

    fun forceHeading(heading: Float) {
        val currentPose = lastPose ?: return
        if (initialPose == null) return
        val currentYaw = extractYawDegrees(currentPose)
        val yawDelta = currentYaw - initialYaw
        initialHeading = (heading + yawDelta).mod(360f)
        headingLocked = true
    }

    fun unlockHeading() {
        headingLocked = false
    }

    fun start() {
        locationTracker.start()
        updateJob = scope.launch {
            combine(
                headingProvider.smoothedValue,
                locationTracker.locationState
            ) { heading, locationResult ->
                heading to locationResult
            }.collect { (heading, location) ->
                lastHeading = heading
                lastLocation = location
            }
        }
        sceneView.onSessionUpdated = { _, frame ->
            val camera = frame.camera
            val pose = camera.pose
            val trackingState = camera.trackingState

            when (trackingState) {
                TrackingState.TRACKING -> {
                    lastPose = pose
                    if (initialPose == null) {
                        initialPose = pose
                        initialHeading = lastHeading
                        initialLocation = lastLocation
                        initialYaw = extractYawDegrees(pose)
                    } else {
                        if (initialHeading == null) initialHeading = lastHeading
                        if (initialLocation == null) initialLocation = lastLocation

                        if (!headingLocked) {
                            val currentHeading = lastHeading
                            if (currentHeading != null) {
                                val currentYaw = extractYawDegrees(pose)
                                val yawDelta = currentYaw - initialYaw
                                initialHeading = (currentHeading + yawDelta).mod(360f)
                            }
                        }
                    }
                }

                TrackingState.PAUSED -> {}

                TrackingState.STOPPED -> {
                    initialPose = null
                    initialHeading = null
                    initialLocation = null
                    initialYaw = 0f
                    lastPose = null
                    headingLocked = false
                }
            }

            val initPose = initialPose
            val initHeading = initialHeading
            val initLocation = initialLocation

            if (trackingState == TrackingState.TRACKING && initPose != null && initHeading != null && initLocation != null) {
                val location = computeLocation(
                    pose = pose,
                    initialPose = initPose,
                    initialHeading = initHeading,
                    initialLocation = initLocation
                )
                headingProvider.setLocation(location)
                onFrameUpdate?.invoke(
                    ArFrameData(
                        userLocation = location,
                        userHeading = lastHeading ?: initHeading,
                        frame = frame,
                        cameraPose = pose,
                        initialCameraHeading = initHeading,
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
        initialPose = null
        initialHeading = null
        initialLocation = null
        initialYaw = 0f
        lastHeading = null
        lastLocation = null
        lastPose = null
        headingLocked = false
    }

    fun computeLocation(pose: Pose): LocationData? {
        val initPose = initialPose
        val initHeading = initialHeading
        val initLocation = initialLocation
        if (initPose == null || initHeading == null || initLocation == null) {
            return null
        }
        return computeLocation(pose, initPose, initHeading, initLocation)
    }

    private fun computeLocation(
        pose: Pose,
        initialPose: Pose,
        initialHeading: Float,
        initialLocation: LocationData,
    ): LocationData {
        val dx = (pose.tx() - initialPose.tx()).toDouble()
        val dy = (pose.ty() - initialPose.ty()).toDouble()
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
            altitude = initialLocation.altitude + dy,
        )
    }

    private fun extractYawDegrees(pose: Pose): Float {
        val qx = pose.qx().toDouble()
        val qy = pose.qy().toDouble()
        val qz = pose.qz().toDouble()
        val qw = pose.qw().toDouble()
        val sinYaw = 2.0 * (qx * qz + qw * qy)
        val cosYaw = 1.0 - 2.0 * (qx * qx + qy * qy)
        return Math.toDegrees(atan2(sinYaw, cosYaw)).toFloat()
    }
}

