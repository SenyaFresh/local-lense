package ru.hse.edu.geoar.main

import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import kotlin.math.cos
import kotlin.math.sin

class ArGeoWallFinder {
    fun raycastWall(frame: Frame, cameraPose: Pose, dirX: Float, dirZ: Float): HitResult? {
        val origin = floatArrayOf(cameraPose.tx(), cameraPose.ty(), cameraPose.tz())

        findVerticalHit(frame, origin, dirX, dirZ)?.let { return it }

        for (angleDeg in intArrayOf(-15, 15, -30, 30)) {
            val rad = Math.toRadians(angleDeg.toDouble())
            val c = cos(rad).toFloat()
            val s = sin(rad).toFloat()
            val rx = dirX * c - dirZ * s
            val rz = dirX * s + dirZ * c
            findVerticalHit(frame, origin, rx, rz)?.let { return it }
        }

        return null
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