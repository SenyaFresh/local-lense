package ru.hse.edu.geoar.location

import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val anchorPersistence: AnchorPersistence? = null,
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
    private var pendingForcedHeading: Float? = null

    private var warmupStartMs: Long = 0L
    private var isWarmedUp = false

    private var migrationStartMs: Long = 0L
    private var migrationLatOffset: Double = 0.0
    private var migrationLonOffset: Double = 0.0
    private var migrationAltOffset: Double = 0.0
    private var migrationHeadingOffset: Float = 0f

    private data class PreLossSnapshot(
        val timestampMs: Long,
        val location: LocationData,
        val heading: Float,
    )

    private var preLossSnapshot: PreLossSnapshot? = null

    private var lastTrackingState: TrackingState? = null
    private var lastPersistedAtMs: Long = 0L

    private val _effectiveUserLocation = MutableStateFlow<LocationData?>(null)
    val effectiveUserLocation: StateFlow<LocationData?> = _effectiveUserLocation.asStateFlow()

    private val _effectiveUserHeading = MutableStateFlow<Float?>(null)
    val effectiveUserHeading: StateFlow<Float?> = _effectiveUserHeading.asStateFlow()

    val isHeadingLocked: Boolean
        get() = headingLocked

    var onFrameUpdate: ((ArFrameData) -> Unit)? = null

    fun forceHeading(heading: Float) {
        val currentPose = lastPose
        if (currentPose != null && initialPose != null && isWarmedUp) {
            val currentYaw = extractYawDegrees(currentPose)
            val yawDelta = currentYaw - initialYaw
            initialHeading = (heading + yawDelta + 360f).mod(360f)
            headingLocked = true
            cancelMigration()
            pendingForcedHeading = null
        } else {
            pendingForcedHeading = heading
            headingLocked = true
        }
    }

    fun unlockHeading() {
        headingLocked = false
        pendingForcedHeading = null
    }

    fun start() {
        locationTracker.start()
        restorePersistedSnapshotAsPreLoss()
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
            handleFrameUpdate(frame)
        }
    }

    fun stop() {
        updateJob?.cancel()
        updateJob = null
        locationTracker.stop()
        sceneView.onSessionUpdated = null
        clearAnchor()
        lastHeading = null
        lastLocation = null
        lastPose = null
        preLossSnapshot = null
        cancelMigration()
        pendingForcedHeading = null
        headingLocked = false
        _effectiveUserLocation.value = null
        _effectiveUserHeading.value = null
    }

    fun computeLocation(pose: Pose): LocationData? {
        val initPose = initialPose ?: return null
        val initHeading = initialHeading ?: return null
        val initLocation = initialLocation ?: return null
        if (!isWarmedUp) return null
        return computeLocation(pose, initPose, initHeading, initLocation)
    }

    private fun handleFrameUpdate(frame: Frame) {
        val camera = frame.camera
        val pose = camera.pose
        val trackingState = camera.trackingState

        val previousState = lastTrackingState
        lastTrackingState = trackingState

        when (trackingState) {
            TrackingState.TRACKING -> {
                lastPose = pose
                if (!isWarmedUp) {
                    tryWarmupAndCommit(pose)
                }
            }

            TrackingState.PAUSED -> {
            }

            TrackingState.STOPPED -> {
                if (previousState != TrackingState.STOPPED) {
                    captureSnapshotForMigration()
                }
                clearAnchor()
            }
        }

        emitFrameIfReady(frame, pose, trackingState)
    }

    private fun tryWarmupAndCommit(pose: Pose) {
        if (warmupStartMs == 0L) {
            warmupStartMs = System.currentTimeMillis()
            return
        }
        val elapsed = System.currentTimeMillis() - warmupStartMs
        if (elapsed < MIN_WARMUP_DURATION_MS) return

        val currentLocation = lastLocation ?: return
        val currentHeading = lastHeading ?: return

        val accuracyMeters = locationTracker.currentAccuracyMeters()
        val gpsConverged = accuracyMeters != null &&
                accuracyMeters <= MAX_ACCEPTABLE_ACCURACY_METERS
        if (!gpsConverged && elapsed < MAX_WARMUP_DURATION_MS) return

        val pendingForce = pendingForcedHeading

        initialPose = pose
        initialYaw = extractYawDegrees(pose)
        initialLocation = currentLocation
        initialHeading = pendingForce ?: currentHeading
        if (pendingForce != null) {
            headingLocked = true
        }
        pendingForcedHeading = null
        isWarmedUp = true

        scheduleMigrationIfApplicable(initialLocation!!, initialHeading!!)
    }

    private fun captureSnapshotForMigration() {
        val effLoc = _effectiveUserLocation.value ?: return
        val effHead = _effectiveUserHeading.value ?: return
        val snapshot = PreLossSnapshot(
            timestampMs = System.currentTimeMillis(),
            location = effLoc,
            heading = effHead,
        )
        preLossSnapshot = snapshot
        anchorPersistence?.save(
            AnchorPersistence.Snapshot(
                timestampMs = snapshot.timestampMs,
                location = snapshot.location,
                heading = snapshot.heading,
            )
        )
    }

    fun persistSnapshotNow() {
        val effLoc = _effectiveUserLocation.value ?: return
        val effHead = _effectiveUserHeading.value ?: return
        anchorPersistence?.save(
            AnchorPersistence.Snapshot(
                timestampMs = System.currentTimeMillis(),
                location = effLoc,
                heading = effHead,
            )
        )
    }

    private fun restorePersistedSnapshotAsPreLoss() {
        if (preLossSnapshot != null) return
        val persisted = anchorPersistence?.load(MAX_PERSISTED_AGE_MS) ?: return
        preLossSnapshot = PreLossSnapshot(
            timestampMs = persisted.timestampMs,
            location = persisted.location,
            heading = persisted.heading,
        )
    }

    private fun scheduleMigrationIfApplicable(
        committedLocation: LocationData,
        committedHeading: Float,
    ) {
        val snapshot = preLossSnapshot ?: return
        preLossSnapshot = null
        val age = System.currentTimeMillis() - snapshot.timestampMs
        if (age > MAX_MIGRATION_AGE_MS) return

        migrationLatOffset = snapshot.location.latitude - committedLocation.latitude
        migrationLonOffset = snapshot.location.longitude - committedLocation.longitude
        migrationAltOffset = snapshot.location.altitude - committedLocation.altitude
        migrationHeadingOffset = signedAngleDelta(snapshot.heading, committedHeading)
        migrationStartMs = System.currentTimeMillis()
    }

    private fun cancelMigration() {
        migrationStartMs = 0L
        migrationLatOffset = 0.0
        migrationLonOffset = 0.0
        migrationAltOffset = 0.0
        migrationHeadingOffset = 0f
    }

    private fun computeMigrationFactor(): Double {
        if (migrationStartMs == 0L) return 0.0
        val elapsed = System.currentTimeMillis() - migrationStartMs
        if (elapsed >= MIGRATION_DURATION_MS) {
            cancelMigration()
            return 0.0
        }
        val linear = 1.0 - elapsed.toDouble() / MIGRATION_DURATION_MS
        return linear * linear
    }

    private fun clearAnchor() {
        initialPose = null
        initialHeading = null
        initialLocation = null
        initialYaw = 0f
        lastPose = null
        isWarmedUp = false
        warmupStartMs = 0L
        cancelMigration()
    }

    private fun emitFrameIfReady(frame: Frame, pose: Pose, trackingState: TrackingState) {
        if (trackingState != TrackingState.TRACKING) return
        val initPose = initialPose ?: return
        val initHeading = initialHeading ?: return
        val initLocation = initialLocation ?: return
        if (!isWarmedUp) return

        val rawLocation = computeLocation(pose, initPose, initHeading, initLocation)
        val yawDelta = extractYawDegrees(pose) - initialYaw

        val factor = computeMigrationFactor()
        val effectiveLocation = LocationData(
            latitude = rawLocation.latitude + migrationLatOffset * factor,
            longitude = rawLocation.longitude + migrationLonOffset * factor,
            altitude = rawLocation.altitude + migrationAltOffset * factor,
        )
        val effectiveInitialHeading =
            ((initHeading + migrationHeadingOffset * factor.toFloat()) + 360f).mod(360f)
        val effectiveHeading = ((effectiveInitialHeading - yawDelta) + 360f).mod(360f)

        _effectiveUserLocation.value = effectiveLocation
        _effectiveUserHeading.value = effectiveHeading

        val nowMs = System.currentTimeMillis()
        if (nowMs - lastPersistedAtMs >= PERSIST_INTERVAL_MS) {
            lastPersistedAtMs = nowMs
            anchorPersistence?.save(
                AnchorPersistence.Snapshot(
                    timestampMs = nowMs,
                    location = effectiveLocation,
                    heading = effectiveHeading,
                )
            )
        }

        headingProvider.setLocation(effectiveLocation)
        onFrameUpdate?.invoke(
            ArFrameData(
                userLocation = effectiveLocation,
                userHeading = effectiveHeading,
                frame = frame,
                cameraPose = pose,
                initialCameraHeading = effectiveInitialHeading,
            )
        )
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

    private fun signedAngleDelta(target: Float, current: Float): Float =
        ((target - current + 540f).mod(360f)) - 180f

    companion object {
        private const val MIN_WARMUP_DURATION_MS = 1500L
        private const val MAX_WARMUP_DURATION_MS = 6000L
        private const val MAX_ACCEPTABLE_ACCURACY_METERS = 8.0
        private const val MIGRATION_DURATION_MS = 1500L
        private const val MAX_MIGRATION_AGE_MS = 30_000L
        private const val PERSIST_INTERVAL_MS = 3_000L
        private const val MAX_PERSISTED_AGE_MS = 5L * 60L * 1000L
    }
}
