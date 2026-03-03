package ru.hse.edu.geoar.main

import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.core.HitResult
import io.github.sceneview.math.Scale
import ru.hse.edu.geoar.geo.GeoObject
import ru.hse.edu.geoar.geo.GeoUtils
import ru.hse.edu.geoar.location.LocationData
import kotlin.math.atan2
import kotlin.math.ln
import io.github.sceneview.math.Position
import kotlin.math.cos
import kotlin.math.sin

class ArGeoObjectController(val geoObject: GeoObject) {
    private enum class State { SEARCHING, ATTACHED }

    private var state: State = State.SEARCHING
    private var anchor: Anchor? = null
    private var wallNormal: FloatArray = FloatArray(3)

    fun update(
        userLocation: LocationData,
        userHeading: Float,
        frame: Frame,
        cameraPose: Pose,
        wallFinder: ArGeoWallFinder
    ) {
        val distance = GeoUtils.distanceMeters(userLocation, geoObject)

        if (distance > ArGeoConfig.MAX_DISTANCE_METERS) {
            geoObject.node.isVisible = false
            return
        }

        geoObject.node.scale = calculateScale(distance)

        when (state) {
            State.ATTACHED -> updateAttachedState()
            State.SEARCHING -> updateSearchingState(userLocation, userHeading, cameraPose, frame, wallFinder, distance)
        }
    }

    fun detachAnchor() {
        anchor?.detach()
        anchor = null
        state = State.SEARCHING
    }

    private fun updateAttachedState() {
        val currentAnchor = anchor
        if (currentAnchor == null || currentAnchor.trackingState != TrackingState.TRACKING) {
            detachAnchor()
            return
        }

        val pose = currentAnchor.pose
        applyWallTransform(pose, wallNormal)
        geoObject.node.isVisible = true
    }

    private fun updateSearchingState(
        userLoc: LocationData,
        userHeading: Float,
        cameraPose: Pose,
        frame: Frame,
        wallFinder: ArGeoWallFinder,
        distance: Double
    ) {
        val bearing = GeoUtils.bearingDegrees(userLoc, geoObject)
        val angleRad = Math.toRadians(bearing - userHeading.toDouble())

        val dirX = sin(angleRad).toFloat()
        val dirZ = -cos(angleRad).toFloat()

        val wallHit = wallFinder.raycastWall(frame, cameraPose, dirX, dirZ)

        if (wallHit != null) {
            attachToWall(wallHit)
        } else if (ArGeoConfig.SHOW_WHILE_SEARCHING) {
            placeInAir(cameraPose, angleRad, distance)
            geoObject.node.isVisible = true
        } else {
            geoObject.node.isVisible = false
        }
    }

    private fun attachToWall(hit: HitResult) {
        anchor = hit.createAnchor()

        hit.hitPose.getTransformedAxis(1, 1f, wallNormal, 0)

        state = State.ATTACHED
        geoObject.node.isVisible = true
    }

    private fun placeInAir(cameraPose: Pose, angleRad: Double, realDistance: Double) {
        val arDistance = compressDistance(realDistance)

        val x = cameraPose.tx() + (sin(angleRad) * arDistance).toFloat()
        val y = cameraPose.ty()
        val z = cameraPose.tz() - (cos(angleRad) * arDistance).toFloat()

        geoObject.node.worldPosition = Position(x, y, z)

        val yaw = Math.toDegrees(atan2((x - cameraPose.tx()).toDouble(), (z - cameraPose.tz()).toDouble())).toFloat()
        geoObject.node.worldRotation = io.github.sceneview.math.Rotation(0f, yaw, 0f)
    }

    private fun applyWallTransform(anchorPose: Pose, normal: FloatArray) {
        val x = anchorPose.tx() + normal[0] * ArGeoConfig.WALL_OFFSET
        val y = anchorPose.ty() + normal[1] * ArGeoConfig.WALL_OFFSET
        val z = anchorPose.tz() + normal[2] * ArGeoConfig.WALL_OFFSET

        geoObject.node.worldPosition = Position(x, y, z)

        val yaw = Math.toDegrees(atan2(normal[0].toDouble(), normal[2].toDouble())).toFloat()
        geoObject.node.worldRotation = io.github.sceneview.math.Rotation(0f, yaw, 0f)
    }

    private fun compressDistance(meters: Double): Double {
        val t = (meters / ArGeoConfig.MAX_DISTANCE_METERS).coerceIn(0.0, 1.0)
        return ln(1.0 + t * ArGeoConfig.LOG_RANGE) / ln(ArGeoConfig.LOG_BASE) * ArGeoConfig.AR_RADIUS
    }

    private fun calculateScale(meters: Double): Scale {
        val factor = (1.0 - meters / ArGeoConfig.MAX_DISTANCE_METERS)
            .coerceIn(ArGeoConfig.MIN_SCALE_FACTOR, 1.0).toFloat()
        val s = factor * ArGeoConfig.BASE_SCALE
        return Scale(s, s, s)
    }
}
