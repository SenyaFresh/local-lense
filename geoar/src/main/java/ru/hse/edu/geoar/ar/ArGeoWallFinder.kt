package ru.hse.edu.geoar.ar

import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import io.github.sceneview.math.Position
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object ArGeoWallFinder {

    private const val MAX_LATERAL_DEVIATION_M = 1.5f

    private val SEARCH_ANGLES_DEG = intArrayOf(
        0,
        -10, 10,
        -20, 20,
        -30, 30,
        -45, 45,
        -60, 60,
    )

    private val RADIAL_ANGLES_DEG = floatArrayOf(
        90f, -90f, 0f, 180f, 45f, -45f, 135f, -135f
    )

    fun searchAroundPosition(
        frame: Frame,
        cameraPose: Pose,
        objectPosition: Position
    ): HitResult? {
        val camX = cameraPose.tx()
        val camZ = cameraPose.tz()

        val baseDirX = objectPosition.x - camX
        val baseDirZ = objectPosition.z - camZ
        val length = sqrt(baseDirX * baseDirX + baseDirZ * baseDirZ)

        if (length == 0f) return null
        val normX = baseDirX / length
        val normZ = baseDirZ / length

        val origin = floatArrayOf(objectPosition.x, cameraPose.ty(), objectPosition.z)

        var bestHit: HitResult? = null
        var minDistance = Float.MAX_VALUE

        for (angleDeg in RADIAL_ANGLES_DEG) {
            val rad = Math.toRadians(angleDeg.toDouble())
            val cos = cos(rad).toFloat()
            val sin = sin(rad).toFloat()

            val dirX = normX * cos - normZ * sin
            val dirZ = normX * sin + normZ * cos

            val hit = raycastFromOrigin(frame, origin, dirX, dirZ)

            if (hit != null && hit.distance < minDistance) {
                minDistance = hit.distance
                bestHit = hit
            }
        }

        return bestHit
    }

    private fun raycastFromOrigin(frame: Frame, origin: FloatArray, dirX: Float, dirZ: Float): HitResult? {
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
                        && hit.distance <= ArGeoConfig.AR_RADIUS
            }
    }
}