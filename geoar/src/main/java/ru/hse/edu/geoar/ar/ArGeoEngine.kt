package ru.hse.edu.geoar.ar

import android.content.Context
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import ru.hse.edu.geoar.location.ArFrameData
import ru.hse.edu.geoar.location.ArPoseLocationTracker
import ru.hse.edu.geoar.location.LocationTracker
import ru.hse.edu.geoar.sensors.HeadingProvider
import ru.hse.edu.geoar.sensors.LinearAccelerationProvider
import ru.hse.edu.geoar.sensors.SensorsManager
import ru.hse.edu.geoar.sensors.StepDetectorProvider
import java.util.concurrent.CopyOnWriteArrayList

class ArGeoEngine(
    private val sceneView: ARSceneView,
    private val scope: CoroutineScope,
    context: Context
) {
    var onTap: ((ArTapResult?) -> Unit)? = null

    private val headingProvider = HeadingProvider(context)
    private val sensorsManager = SensorsManager(
        headingProvider = headingProvider,
        stepDetectorProvider = StepDetectorProvider(context),
        linearAccelerationProvider = LinearAccelerationProvider(context),
    )
    private val locationTracker = LocationTracker(
        sensorsManager = sensorsManager,
        scope = scope,
        context = context
    )
    private val arPoseLocationTracker = ArPoseLocationTracker(
        headingProvider = headingProvider,
        sceneView = sceneView,
        locationTracker = locationTracker,
        scope = scope
    )
    private val controllers = CopyOnWriteArrayList<ArGeoObjectController>()
    private var isRunning = false

    init {
        sceneView.planeRenderer.isEnabled = true
        sceneView.planeRenderer.isVisible = true

        ensureRunning()

        sceneView.setOnGestureListener(
            onSingleTapConfirmed = { e, _ ->
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

    private fun stop() {
        isRunning = false
        arPoseLocationTracker.onFrameUpdate = null
        arPoseLocationTracker.stop()
    }
}