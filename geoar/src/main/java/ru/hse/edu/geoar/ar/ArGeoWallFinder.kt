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

    private const val MAX_LATERAL_DEVIATION_METERS = 1.5f

    private val SEARCH_ANGLES_DEGREES = intArrayOf(
        0,
        -10, 10,
        -20, 20,
        -30, 30,
        -45, 45,
        -60, 60,
    )

    private val RADIAL_ANGLES_DEGREES = floatArrayOf(
        90f, -90f, 0f, 180f, 45f, -45f, 135f, -135f
    )

    fun searchAroundPosition(
        frame: Frame,
        cameraPose: Pose,
        objectPosition: Position
    ): HitResult? {
        val cameraX = cameraPose.tx()
        val cameraZ = cameraPose.tz()

        val baseDirectionX = objectPosition.x - cameraX
        val baseDirectionZ = objectPosition.z - cameraZ
        val length = sqrt(baseDirectionX * baseDirectionX + baseDirectionZ * baseDirectionZ)

        if (length == 0f) return null
        val normalizedX = baseDirectionX / length
        val normalizedZ = baseDirectionZ / length

        val origin = floatArrayOf(objectPosition.x, cameraPose.ty(), objectPosition.z)

        var bestHit: HitResult? = null
        var minDistance = Float.MAX_VALUE

        for (angleDegrees in RADIAL_ANGLES_DEGREES) {
            val radians = Math.toRadians(angleDegrees.toDouble())
            val cosValue = cos(radians).toFloat()
            val sinValue = sin(radians).toFloat()

            val directionX = normalizedX * cosValue - normalizedZ * sinValue
            val directionZ = normalizedX * sinValue + normalizedZ * cosValue

            val hit = raycastFromOrigin(frame, origin, directionX, directionZ)

            if (hit != null && hit.distance < minDistance) {
                minDistance = hit.distance
                bestHit = hit
            }
        }

        return bestHit
    }

    private fun raycastFromOrigin(frame: Frame, origin: FloatArray, directionX: Float, directionZ: Float): HitResult? {
        for (angleDegrees in SEARCH_ANGLES_DEGREES) {
            val (rotatedX, rotatedZ) = rotateDirection(directionX, directionZ, angleDegrees)
            val hit = findVerticalHit(frame, origin, rotatedX, rotatedZ) ?: continue

            if (isAngleAcceptableForDistance(angleDegrees, hit.distance)) {
                return hit
            }
        }
        return null
    }

    private fun isAngleAcceptableForDistance(angleDegrees: Int, distance: Float): Boolean {
        if (angleDegrees == 0) return true
        val maxAngleDegrees = Math.toDegrees(
            atan(MAX_LATERAL_DEVIATION_METERS / distance).toDouble()
        ).toFloat()
        return abs(angleDegrees) <= maxAngleDegrees
    }

    private fun rotateDirection(
        directionX: Float,
        directionZ: Float,
        angleDegrees: Int
    ): Pair<Float, Float> {
        if (angleDegrees == 0) return directionX to directionZ
        val radians = Math.toRadians(angleDegrees.toDouble())
        val cosValue = cos(radians).toFloat()
        val sinValue = sin(radians).toFloat()
        return (directionX * cosValue - directionZ * sinValue) to (directionX * sinValue + directionZ * cosValue)
    }

    private fun findVerticalHit(
        frame: Frame,
        origin: FloatArray,
        deltaX: Float,
        deltaZ: Float
    ): HitResult? {
        val direction = floatArrayOf(deltaX, 0f, deltaZ)
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