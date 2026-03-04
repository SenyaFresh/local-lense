package ru.hse.edu.geoar.main

import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin

class ArGeoWallFinder {

    companion object {
        private const val MAX_LATERAL_DEVIATION_M = 1.5f

        private val SEARCH_ANGLES_DEG = intArrayOf(
            0,
            -10, 10,
            -20, 20,
            -30, 30,
            -45, 45,
            -60, 60
        )
    }

    fun raycastWall(frame: Frame, cameraPose: Pose, dirX: Float, dirZ: Float): HitResult? {
        val origin = floatArrayOf(cameraPose.tx(), cameraPose.ty(), cameraPose.tz())

        for (angleDeg in SEARCH_ANGLES_DEG) {
            val (rx, rz) = rotateDirection(dirX, dirZ, angleDeg)
            val hit = findVerticalHit(frame, origin, rx, rz) ?: continue

            if (isAngleAcceptableForDistance(angleDeg, hit.distance)) {
                return hit
            }
        }

        return null
    }

    private fun isAngleAcceptableForDistance(angleDeg: Int, distance: Float): Boolean {
        if (angleDeg == 0) return true
        val maxAngleDeg = Math.toDegrees(
            atan(MAX_LATERAL_DEVIATION_M / distance).toDouble()
        ).toFloat()
        return abs(angleDeg) <= maxAngleDeg
    }

    private fun rotateDirection(
        dirX: Float,
        dirZ: Float,
        angleDeg: Int
    ): Pair<Float, Float> {
        if (angleDeg == 0) return dirX to dirZ
        val rad = Math.toRadians(angleDeg.toDouble())
        val c = cos(rad).toFloat()
        val s = sin(rad).toFloat()
        return (dirX * c - dirZ * s) to (dirX * s + dirZ * c)
    }

    private fun findVerticalHit(
        frame: Frame,
        origin: FloatArray,
        dx: Float,
        dz: Float
    ): HitResult? {
        val direction = floatArrayOf(dx, 0f, dz)
        return frame.hitTest(origin, 0, direction, 0)
            .firstOrNull { hit ->
                val trackable = hit.trackable
                trackable is Plane
                        && trackable.type == Plane.Type.VERTICAL
                        && trackable.trackingState == TrackingState.TRACKING
                        && hit.distance <= ArGeoConfig.MAX_WALL_DETECTION_DISTANCE
            }
    }
}