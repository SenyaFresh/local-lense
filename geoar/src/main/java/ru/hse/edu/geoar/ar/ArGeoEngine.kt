package ru.hse.edu.geoar.ar

import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.hse.edu.geoar.location.ArFrameData
import ru.hse.edu.geoar.location.ArPoseLocationTracker
import ru.hse.edu.geoar.math.GeoMath
import java.util.concurrent.CopyOnWriteArrayList

enum class ArGeoEngineMode {
    PLACEMENT,
    VIEW
}

class ArGeoEngine(
    private val sceneView: ARSceneView,
    private val scope: CoroutineScope,
    initialMode: ArGeoEngineMode = ArGeoEngineMode.VIEW
) {
    var onTap: ((ArTapResult?) -> Unit)? = null

    var mode: ArGeoEngineMode = initialMode
        set(value) {
            if (field == value) return
            field = value
            applyMode(value)
        }

    private val headingProvider = ArGeoFactory.headingProvider
    private val sensorsManager = ArGeoFactory.sensorsManager
    private val locationTracker = ArGeoFactory.locationTracker
    val arPoseLocationTracker = ArPoseLocationTracker(
        headingProvider = headingProvider,
        sceneView = sceneView,
        locationTracker = locationTracker,
        scope = scope,
        anchorPersistence = ArGeoFactory.anchorPersistence,
    ).also { ArGeoFactory.activeArPoseLocationTracker = it }
    private val controllers = CopyOnWriteArrayList<ArGeoObjectController>()
    private var isRunning = false
    private var visibilityJob: Job? = null

    private val _placedMarkers = MutableStateFlow<List<PlacedMarkerSnapshot>>(emptyList())
    val placedMarkers: StateFlow<List<PlacedMarkerSnapshot>> = _placedMarkers.asStateFlow()

    init {
        sceneView.planeRenderer.isEnabled = true
        applyMode(initialMode)

        ensureRunning()

        sceneView.setOnGestureListener(
            onSingleTapConfirmed = { e, _ ->
                if (mode != ArGeoEngineMode.PLACEMENT) return@setOnGestureListener

                val frame = sceneView.frame ?: return@setOnGestureListener
                val camera = frame.camera

                if (camera.trackingState != TrackingState.TRACKING) {
                    onTap?.invoke(null)
                    return@setOnGestureListener
                }

                val hitResult = sceneView.hitTestAR(
                    xPx = e.x,
                    yPx = e.y,
                    planeTypes = setOf(
                        Plane.Type.HORIZONTAL_UPWARD_FACING,
                        Plane.Type.HORIZONTAL_DOWNWARD_FACING,
                        Plane.Type.VERTICAL
                    )
                )

                if (hitResult == null) {
                    onTap?.invoke(null)
                    return@setOnGestureListener
                }

                val hitPose = hitResult.hitPose

                arPoseLocationTracker.computeLocation(hitPose)?.let { location ->
                    val isWall = (hitResult.trackable as? Plane)?.type == Plane.Type.VERTICAL
                    onTap?.invoke(ArTapResult(location, isWall))
                }
            }
        )
    }

    fun place(arGeoObject: ArGeoObject): StateFlow<ArGeoObjectPlacementResult?> {
        val controller = ArGeoObjectController(arGeoObject)
        controllers.add(controller)
        arGeoObject.node.isVisible = false
        sceneView.addChildNode(arGeoObject.node)
        ensureRunning()
        return controller.info
    }

    fun clear() {
        controllers.forEach {
            it.detach()
            sceneView.removeChildNode(it.arGeoObject.node)
        }
        controllers.clear()
        _placedMarkers.value = emptyList()
    }

    private fun applyMode(newMode: ArGeoEngineMode) {
        when (newMode) {
            ArGeoEngineMode.PLACEMENT -> {
                sceneView.planeRenderer.isVisible = true
            }

            ArGeoEngineMode.VIEW -> {
                sceneView.planeRenderer.isVisible = false
            }
        }
    }

    private fun ensureRunning() {
        if (isRunning) return
        isRunning = true
        arPoseLocationTracker.onFrameUpdate = { frameData -> processFrame(frameData) }
        arPoseLocationTracker.start()
        visibilityJob = scope.launch {
            arPoseLocationTracker.trackingState.collect { state ->
                if (state != TrackingState.TRACKING) {
                    for (controller in controllers) {
                        controller.arGeoObject.node.isVisible = false
                    }
                }
            }
        }
    }

    private fun processFrame(frameData: ArFrameData) {
        for (controller in controllers) {
            controller.update(
                userLocation = frameData.userLocation,
                userHeading = frameData.userHeading,
                frame = frameData.frame,
                cameraPose = frameData.cameraPose,
                initialCameraHeading = frameData.initialCameraHeading,
            )
        }
        _placedMarkers.value = controllers.map { controller ->
            val info = controller.info.value
            val obj = controller.arGeoObject
            val screenBearing = GeoMath.relativeBearingDegrees(frameData.cameraPose, obj.node)
            val distance = info?.distanceMeters
                ?: GeoMath.distanceMeters(frameData.userLocation, obj)
            PlacedMarkerSnapshot(
                id = obj.id,
                locationData = obj.locationData,
                distanceMeters = distance,
                screenBearingDegrees = screenBearing,
                altitudeDifferenceMeters = obj.locationData.altitude - frameData.userLocation.altitude,
            )
        }
    }

    fun stop() {
        isRunning = false
        visibilityJob?.cancel()
        visibilityJob = null
        arPoseLocationTracker.onFrameUpdate = null
        arPoseLocationTracker.stop()
        if (ArGeoFactory.activeArPoseLocationTracker === arPoseLocationTracker) {
            ArGeoFactory.activeArPoseLocationTracker = null
        }
    }
}