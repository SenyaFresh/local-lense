package ru.hse.edu.geoar.main

import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import ru.hse.edu.geoar.geo.GeoObject
import ru.hse.edu.geoar.heading.HeadingProvider
import ru.hse.edu.geoar.location.LocationData
import ru.hse.edu.geoar.location.LocationTracker
import java.util.concurrent.CopyOnWriteArrayList

class ArGeoEngine(
    private val sceneView: ARSceneView,
    private val locationTracker: LocationTracker,
    private val headingProvider: HeadingProvider,
    private val scope: CoroutineScope
) {
    private val controllers = CopyOnWriteArrayList<ArGeoObjectController>()
    private var job: Job? = null

    private val wallFinder = ArGeoWallFinder()

    fun place(geoObject: GeoObject) {
        val controller = ArGeoObjectController(geoObject)
        controllers.add(controller)
        sceneView.addChildNode(geoObject.node)
        ensureRunning()
    }

    fun remove(geoObject: GeoObject) {
        val controller = controllers.find { it.geoObject == geoObject } ?: return
        controller.detachAnchor()
        sceneView.removeChildNode(controller.geoObject.node)
        controllers.remove(controller)

        if (controllers.isEmpty()) stop()
    }

    fun clear() {
        controllers.forEach {
            it.detachAnchor()
            sceneView.removeChildNode(it.geoObject.node)
        }
        controllers.clear()
        stop()
    }

    private fun ensureRunning() {
        if (job != null) return
        headingProvider.start()
        locationTracker.start()

        job = scope.launch {
            combine(
                locationTracker.locationState.filterNotNull(),
                headingProvider.heading
            ) { loc, heading -> loc to heading }
                .collect { (locResult, heading) ->
                    val frame = sceneView.frame ?: return@collect
                    val loc = locResult.unwrapOrNull() ?: return@collect
                    val camera = frame.camera

                    if (camera.trackingState == TrackingState.TRACKING) {
                        updateControllers(loc, heading, frame, camera.pose)
                    }
                }
        }
    }

    private fun stop() {
        job?.cancel()
        job = null
        headingProvider.stop()
        locationTracker.stop()
    }

    private fun updateControllers(
        userLocation: LocationData,
        userHeading: Float,
        frame: Frame,
        cameraPose: Pose
    ) {
        for (controller in controllers) {
            controller.update(
                userLocation = userLocation,
                userHeading = userHeading,
                frame = frame,
                cameraPose = cameraPose,
                wallFinder = wallFinder
            )
        }
    }
}