package ru.hse.edu.geoar.ar

import android.content.Context
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import ru.hse.edu.geoar.geo.GeoObject
import ru.hse.edu.geoar.location.LocationData
import ru.hse.edu.geoar.location.LocationTracker
import ru.hse.edu.geoar.sensors.HeadingProvider
import ru.hse.edu.geoar.sensors.LinearAccelerationProvider
import ru.hse.edu.geoar.sensors.StepDetectorProvider
import ru.hse.locallense.common.ResultContainer
import java.util.concurrent.CopyOnWriteArrayList

class ArGeoEngine(
    private val sceneView: ARSceneView,
    private val scope: CoroutineScope,
    context: Context
) {
    private val headingProvider = HeadingProvider(context)
    private val linearAccelerationProvider = LinearAccelerationProvider(context)
    private val stepDetectorProvider = StepDetectorProvider(context)
    private val locationTracker = LocationTracker(headingProvider, stepDetectorProvider, linearAccelerationProvider, scope, context)
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
        controller.detach()
        sceneView.removeChildNode(controller.geoObject.node)
        controllers.remove(controller)

        if (controllers.isEmpty()) stop()
    }

    fun clear() {
        controllers.forEach {
            it.detach()
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
                headingProvider.smoothed
            ) { loc, heading -> loc to heading }
                .collect { (locResult, heading) ->
                    processFrame(locResult, heading)
                }
        }
    }

    private fun processFrame(locResult: ResultContainer<LocationData>, heading: Float) {
        val frame = sceneView.frame ?: return
        val loc = locResult.unwrapOrNull() ?: return
        val camera = frame.camera

        if (camera.trackingState != TrackingState.TRACKING) return

        for (controller in controllers) {
            controller.update(
                userLocation = loc,
                userHeading = heading,
                frame = frame,
                cameraPose = camera.pose,
                wallFinder = wallFinder
            )
        }
    }

    private fun stop() {
        job?.cancel()
        job = null
        headingProvider.stop()
        locationTracker.stop()
    }
}