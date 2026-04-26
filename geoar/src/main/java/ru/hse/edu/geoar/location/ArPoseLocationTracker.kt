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
import ru.hse.edu.geoar.sensors.HeadingProvider
import ru.hse.locallense.common.entities.LocationData

private const val PERSIST_INTERVAL_MS = 3_000L

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

    private val warmup = WarmupController(locationTracker)
    private val migration = HeadingMigration()
    private val snapshotStore = PreLossSnapshotStore(anchorPersistence)

    private var anchored: AnchoredFrame? = null

    private var lastHeading: Float? = null
    private var lastLocation: LocationData? = null
    private var lastPose: Pose? = null

    private var headingLocked = false
    private var pendingForcedHeading: Float? = null

    private var lastTrackingState: TrackingState? = null
    private var lastPersistedAtMs: Long = 0L
    private var sensorJob: Job? = null

    private val _effectiveUserLocation = MutableStateFlow<LocationData?>(null)
    val effectiveUserLocation: StateFlow<LocationData?> = _effectiveUserLocation.asStateFlow()

    private val _effectiveUserHeading = MutableStateFlow<Float?>(null)
    val effectiveUserHeading: StateFlow<Float?> = _effectiveUserHeading.asStateFlow()

    private val _trackingState = MutableStateFlow<TrackingState?>(null)
    val trackingState: StateFlow<TrackingState?> = _trackingState.asStateFlow()

    val isHeadingLocked: Boolean get() = headingLocked

    var onFrameUpdate: ((ArFrameData) -> Unit)? = null

    fun start() {
        locationTracker.start()
        snapshotStore.restoreFromDisk()
        sensorJob = scope.launch {
            combine(
                headingProvider.smoothedValue,
                locationTracker.locationState,
            ) { heading, location -> heading to location }
                .collect { (heading, location) ->
                    lastHeading = heading
                    lastLocation = location
                }
        }
        sceneView.onSessionUpdated = { _, frame -> handleFrameUpdate(frame) }
    }

    fun stop() {
        sensorJob?.cancel()
        sensorJob = null
        locationTracker.stop()
        sceneView.onSessionUpdated = null
        clearAnchor()
        lastHeading = null
        lastLocation = null
        lastPose = null
        snapshotStore.clear()
        migration.cancel()
        pendingForcedHeading = null
        headingLocked = false
        _effectiveUserLocation.value = null
        _effectiveUserHeading.value = null
        _trackingState.value = null
        lastTrackingState = null
    }

    fun forceHeading(heading: Float) {
        val pose = lastPose
        val anchor = anchored
        if (pose != null && anchor != null && warmup.isCommitted) {
            val yawDelta = extractYawDegrees(pose) - anchor.yaw
            anchored = anchor.copy(heading = (heading + yawDelta + 360f).mod(360f))
            migration.cancel()
            pendingForcedHeading = null
        } else {
            pendingForcedHeading = heading
        }
        headingLocked = true
    }

    fun unlockHeading() {
        headingLocked = false
        pendingForcedHeading = null
    }

    fun computeLocation(pose: Pose): LocationData? {
        val anchor = anchored ?: return null
        if (!warmup.isCommitted) return null
        return poseToLocation(pose, anchor.pose, anchor.heading, anchor.location)
    }

    fun persistSnapshotNow() {
        snapshotStore.persistNow(_effectiveUserLocation.value, _effectiveUserHeading.value)
    }

    private fun handleFrameUpdate(frame: Frame) {
        val camera = frame.camera
        val pose = camera.pose
        val trackingState = camera.trackingState
        _trackingState.value = trackingState

        val previousState = lastTrackingState
        lastTrackingState = trackingState

        when (trackingState) {
            TrackingState.TRACKING -> {
                lastPose = pose
                if (!warmup.isCommitted && warmup.shouldCommit()) commitAnchor(pose)
                emitFrameIfReady(frame, pose)
            }
            TrackingState.PAUSED -> Unit
            TrackingState.STOPPED -> {
                if (previousState != TrackingState.STOPPED) {
                    snapshotStore.captureFromCurrent(
                        _effectiveUserLocation.value,
                        _effectiveUserHeading.value,
                    )
                }
                clearAnchor()
            }
        }
    }

    private fun commitAnchor(pose: Pose) {
        val location = lastLocation ?: return
        val heading = lastHeading ?: return

        val pendingForce = pendingForcedHeading
        val resolvedHeading = pendingForce ?: heading
        if (pendingForce != null) headingLocked = true
        pendingForcedHeading = null

        anchored = AnchoredFrame(
            pose = pose,
            heading = resolvedHeading,
            location = location,
            yaw = extractYawDegrees(pose),
        )
        warmup.markCommitted()

        val snapshot = snapshotStore.take() ?: return
        if (System.currentTimeMillis() - snapshot.timestampMs <= MAX_MIGRATION_AGE_MS) {
            migration.schedule(snapshot, location, resolvedHeading)
        }
    }

    private fun emitFrameIfReady(frame: Frame, pose: Pose) {
        val anchor = anchored ?: return
        if (!warmup.isCommitted) return

        val rawLocation = poseToLocation(pose, anchor.pose, anchor.heading, anchor.location)
        val migrated = migration.apply(rawLocation, anchor.heading)
        val yawDelta = extractYawDegrees(pose) - anchor.yaw
        val effectiveHeading = ((migrated.heading - yawDelta) + 360f).mod(360f)

        _effectiveUserLocation.value = migrated.location
        _effectiveUserHeading.value = effectiveHeading

        maybePersist(migrated.location, effectiveHeading)

        headingProvider.setLocation(migrated.location)
        onFrameUpdate?.invoke(
            ArFrameData(
                userLocation = migrated.location,
                userHeading = effectiveHeading,
                frame = frame,
                cameraPose = pose,
                initialCameraHeading = migrated.heading,
            )
        )
    }

    private fun maybePersist(location: LocationData, heading: Float) {
        val nowMs = System.currentTimeMillis()
        if (nowMs - lastPersistedAtMs < PERSIST_INTERVAL_MS) return
        lastPersistedAtMs = nowMs
        anchorPersistence?.save(
            AnchorPersistence.Snapshot(
                timestampMs = nowMs,
                location = location,
                heading = heading,
            )
        )
    }

    private fun clearAnchor() {
        anchored = null
        lastPose = null
        warmup.reset()
        migration.cancel()
    }

    private data class AnchoredFrame(
        val pose: Pose,
        val heading: Float,
        val location: LocationData,
        val yaw: Float,
    )

    private companion object {
        const val MAX_MIGRATION_AGE_MS = 30_000L
    }
}
