package ru.hse.edu.geoar.main

import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import ru.hse.edu.geoar.geo.GeoObject
import ru.hse.edu.geoar.geo.GeoUtils
import ru.hse.edu.geoar.geo.GeoUtils.calculateScale
import ru.hse.edu.geoar.geo.GeoUtils.compressDistance
import ru.hse.edu.geoar.location.LocationData
import ru.hse.edu.geoar.main.ArGeoConfig.GPS_DRIFT_THRESHOLD_METERS
import ru.hse.edu.geoar.main.ArGeoConfig.WALL_RECHECK_INTERVAL_MS
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ArGeoObjectController(val geoObject: GeoObject) {

    private enum class State {
        SEARCHING,
        ATTACHED_WALL,
        PLACED_AIR
    }

    private var state = State.SEARCHING
    private var anchor: Anchor? = null
    private var wallNormal = FloatArray(3)

    private var fixedPosition: Position? = null
    private var fixedRotation: Rotation? = null

    private var lastWallRecheckTime = 0L
    private var placedUserLocation: LocationData? = null

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
            State.ATTACHED_WALL -> onAttachedWall()

            State.PLACED_AIR -> onPlacedAir(
                userLocation, cameraPose, frame, wallFinder
            )

            State.SEARCHING -> onSearching(
                userLocation, userHeading, cameraPose, frame, wallFinder, distance
            )
        }
    }

    fun detachAnchor() {
        anchor?.detach()
        anchor = null
        fixedPosition = null
        fixedRotation = null
        placedUserLocation = null
        state = State.SEARCHING
    }

    private fun onAttachedWall() {
        val a = anchor
        if (a == null || a.trackingState != TrackingState.TRACKING) {
            detachAnchor()
            return
        }
        applyWallTransform(a.pose, wallNormal)
        geoObject.node.isVisible = true
    }

    private fun onSearching(
        userLoc: LocationData,
        userHeading: Float,
        cameraPose: Pose,
        frame: Frame,
        wallFinder: ArGeoWallFinder,
        distance: Double
    ) {
        val (dirX, dirZ) = computeWorldDirection(userLoc, userHeading, cameraPose)
        val wallHit = wallFinder.raycastWall(frame, cameraPose, dirX, dirZ)

        if (wallHit != null) {
            attachToWall(wallHit)
            return
        }

        snapToAir(cameraPose, dirX, dirZ, distance)
        placedUserLocation = userLoc
        state = State.PLACED_AIR
        lastWallRecheckTime = System.currentTimeMillis()
    }

    private fun onPlacedAir(
        userLoc: LocationData,
        cameraPose: Pose,
        frame: Frame,
        wallFinder: ArGeoWallFinder
    ) {
        val placedLoc = placedUserLocation
        if (placedLoc != null && hasGpsDrifted(placedLoc, userLoc)) {
            state = State.SEARCHING
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastWallRecheckTime > WALL_RECHECK_INTERVAL_MS) {
            lastWallRecheckTime = now
            val dir = directionToFixed(cameraPose)
            if (dir != null) {
                val wallHit = wallFinder.raycastWall(frame, cameraPose, dir.first, dir.second)
                if (wallHit != null) {
                    attachToWall(wallHit)
                    return
                }
            }
        }

        geoObject.node.isVisible = true
    }

    private fun directionToFixed(cameraPose: Pose): Pair<Float, Float>? {
        val pos = fixedPosition ?: return null
        val dx = pos.x - cameraPose.tx()
        val dz = pos.z - cameraPose.tz()
        val len = sqrt(dx * dx + dz * dz)
        return if (len > 1e-4f) Pair(dx / len, dz / len) else null
    }

    private fun hasGpsDrifted(from: LocationData, to: LocationData): Boolean {
        return GeoUtils.distanceMeters(from, to) > GPS_DRIFT_THRESHOLD_METERS
    }

    private fun computeWorldDirection(
        userLoc: LocationData,
        userHeading: Float,
        cameraPose: Pose
    ): Pair<Float, Float> {
        val bearing = GeoUtils.relativeBearing(userHeading, userLoc, geoObject)

        val fwd = FloatArray(3)
        cameraPose.getTransformedAxis(2, -1f, fwd, 0)

        val right = FloatArray(3)
        cameraPose.getTransformedAxis(0, 1f, right, 0)

        val cosA = cos(bearing).toFloat()
        val sinA = sin(bearing).toFloat()

        val dx = fwd[0] * cosA + right[0] * sinA
        val dz = fwd[2] * cosA + right[2] * sinA

        val len = sqrt(dx * dx + dz * dz)
        return if (len > 1e-4f) Pair(dx / len, dz / len) else Pair(0f, -1f)
    }

    private fun snapToAir(
        cameraPose: Pose,
        dirX: Float,
        dirZ: Float,
        realDistance: Double
    ) {
        val arDist = compressDistance(realDistance).toFloat()

        val x = cameraPose.tx() + dirX * arDist
        val y = cameraPose.ty()
        val z = cameraPose.tz() + dirZ * arDist

        fixedPosition = Position(x, y, z)
        fixedRotation = Rotation(
            0f,
            Math.toDegrees(atan2(dirX.toDouble(), dirZ.toDouble())).toFloat(),
            0f
        )

        geoObject.node.worldPosition = fixedPosition!!
        geoObject.node.worldRotation = fixedRotation!!
        geoObject.node.isVisible = true
    }

    private fun attachToWall(hit: HitResult) {
        anchor?.detach()
        anchor = hit.createAnchor()
        hit.hitPose.getTransformedAxis(1, 1f, wallNormal, 0)

        fixedPosition = null
        fixedRotation = null
        placedUserLocation = null
        state = State.ATTACHED_WALL
        geoObject.node.isVisible = true
    }

    private fun applyWallTransform(pose: Pose, normal: FloatArray) {
        geoObject.node.worldPosition = Position(
            pose.tx() + normal[0] * ArGeoConfig.WALL_OFFSET,
            pose.ty() + normal[1] * ArGeoConfig.WALL_OFFSET,
            pose.tz() + normal[2] * ArGeoConfig.WALL_OFFSET
        )
        geoObject.node.worldRotation = Rotation(
            0f,
            Math.toDegrees(atan2(normal[0].toDouble(), normal[2].toDouble())).toFloat(),
            0f
        )
    }
}