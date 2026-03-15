package ru.hse.edu.geoar.ar

import android.content.Context
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import ru.hse.edu.geoar.ar.ArGeoFactory.headingProvider
import ru.hse.edu.geoar.ar.ArGeoFactory.locationTracker
import ru.hse.edu.geoar.location.ArFrameData
import ru.hse.edu.geoar.location.ArPoseLocationTracker
import ru.hse.edu.geoar.location.LocationTracker
import ru.hse.edu.geoar.sensors.HeadingProvider
import ru.hse.edu.geoar.sensors.LinearAccelerationProvider
import ru.hse.edu.geoar.sensors.SensorsManager
import ru.hse.edu.geoar.sensors.StepDetectorProvider
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
        scope = scope
    )
    private val controllers = CopyOnWriteArrayList<ArGeoObjectController>()
    private var isRunning = false

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
    }

    fun stop() {
        isRunning = false
        arPoseLocationTracker.onFrameUpdate = null
        arPoseLocationTracker.stop()
    }
}