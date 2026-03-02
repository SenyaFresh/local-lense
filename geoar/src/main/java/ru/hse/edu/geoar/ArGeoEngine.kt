package ru.hse.edu.geoar

import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.hse.edu.geoar.geo.GeoObject
import ru.hse.edu.geoar.geo.GeoUtils
import ru.hse.edu.geoar.heading.HeadingProvider
import ru.hse.edu.geoar.location.LocationData
import ru.hse.edu.geoar.location.LocationTracker
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin
import io.github.sceneview.math.Rotation
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.atan2

class ArGeoEngine(
    private val sceneView: ARSceneView,
    private val locationTracker: LocationTracker,
    private val headingProvider: HeadingProvider,
    private val scope: CoroutineScope
) {
    var maxDistanceMeters = 500.0
    var arRadius = 10f

    private val geoObjects = CopyOnWriteArrayList<GeoObject>()
    private var job: Job? = null

    fun place(obj: GeoObject) {
        geoObjects.add(obj)
        sceneView.addChildNode(obj.node)
        ensureRunning()
    }

    fun remove(obj: GeoObject) {
        geoObjects.remove(obj)
        sceneView.removeChildNode(obj.node)
        if (geoObjects.isEmpty()) stop()
    }

    fun clear() {
        geoObjects.forEach { sceneView.removeChildNode(it.node) }
        geoObjects.clear()
        stop()
    }

    private fun ensureRunning() {
        if (job != null) return
        headingProvider.start()
        locationTracker.start()
        job = scope.launch {
            combine(
                locationTracker.locationState,
                headingProvider.heading
            ) { loc, heading -> loc to heading }
                .collect { (locResult, heading) ->
                    val loc = locResult.unwrapOrNull() ?: return@collect
                    val camera = sceneView.frame?.camera ?: return@collect
                    if (camera.trackingState != TrackingState.TRACKING) return@collect
                    updatePositions(loc, heading, camera.pose)
                }
        }
    }

    private fun stop() {
        job?.cancel()
        job = null
        headingProvider.stop()
        locationTracker.stop()
    }

    private fun updatePositions(
        location: LocationData,
        heading: Float,
        cameraPose: Pose
    ) {
        for (geoObject in geoObjects) {
            val distance = GeoUtils.distanceMeters(location, geoObject)

            if (distance > maxDistanceMeters) {
                geoObject.node.isVisible = false
                continue
            }
            geoObject.node.isVisible = true

            val bearing = GeoUtils.bearingDegrees(location, geoObject)
            val angle = Math.toRadians(bearing - heading.toDouble())
            val arDist = compressDistance(distance)

            val nodeX = cameraPose.tx() + (sin(angle) * arDist).toFloat()
            val nodeY = cameraPose.ty()
            val nodeZ = cameraPose.tz() - (cos(angle) * arDist).toFloat()

            geoObject.node.worldPosition = Position(nodeX, nodeY, nodeZ)

            val dx = nodeX - cameraPose.tx()
            val dz = nodeZ - cameraPose.tz()
            val yawDeg = Math.toDegrees(atan2(dx.toDouble(), dz.toDouble())).toFloat()
            geoObject.node.worldRotation = Rotation(0f, yawDeg, 0f)

            geoObject.node.scale = scaleFor(distance)
        }
    }

    private fun compressDistance(meters: Double): Double {
        val t = (meters / maxDistanceMeters).coerceIn(0.0, 1.0)
        return ln(1.0 + t * LOG_RANGE) / ln(LOG_BASE) * arRadius
    }

    private fun scaleFor(meters: Double): Scale {
        val factor = (1.0 - meters / maxDistanceMeters)
            .coerceIn(MIN_SCALE_FACTOR, 1.0)
            .toFloat()
        val s = factor * BASE_SCALE
        return Scale(s, s, s)
    }

    private companion object {
        const val BASE_SCALE = 0.2f
        const val MIN_SCALE_FACTOR = 0.3
        const val LOG_BASE = 10.0
        const val LOG_RANGE = 9.0
    }
}