package ru.hse.edu.geoar.math

import com.google.ar.core.Pose
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import ru.hse.edu.geoar.ar.ArGeoConfig
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt

data class Direction2D(val x: Float, val z: Float)

object ArMath {

    fun worldDirection(
        cameraPose: Pose,
        relativeBearingRadians: Double
    ): Direction2D {
        val forward = FloatArray(3)
        cameraPose.getTransformedAxis(2, -1f, forward, 0)

        val right = FloatArray(3)
        cameraPose.getTransformedAxis(0, 1f, right, 0)

        val cosAngle = cos(relativeBearingRadians).toFloat()
        val sinAngle = sin(relativeBearingRadians).toFloat()

        val deltaX = forward[0] * cosAngle + right[0] * sinAngle
        val deltaZ = forward[2] * cosAngle + right[2] * sinAngle

        val length = sqrt(deltaX * deltaX + deltaZ * deltaZ)
        return if (length > 1e-4f) Direction2D(deltaX / length, deltaZ / length)
        else Direction2D(0f, -1f)
    }

    fun yawDegrees(deltaX: Float, deltaZ: Float): Float =
        Math.toDegrees(atan2(deltaX.toDouble(), deltaZ.toDouble())).toFloat()

    fun yawRotation(deltaX: Float, deltaZ: Float): Rotation =
        Rotation(0f, yawDegrees(deltaX, deltaZ), 0f)

    fun airPosition(
        cameraPose: Pose,
        direction: Direction2D,
        realDistanceMeters: Double
    ): Position {
        val arDistance = compressDistance(realDistanceMeters)
        return Position(
            cameraPose.tx() + direction.x * arDistance,
            floor(cameraPose.ty()),
            cameraPose.tz() + direction.z * arDistance
        )
    }

    fun wallPosition(
        anchorPose: Pose,
        normal: FloatArray,
        offset: Float
    ): Position = Position(
        anchorPose.tx() + normal[0] * offset,
        floor(anchorPose.ty()),
        anchorPose.tz() + normal[2] * offset
    )

    fun wallRotation(normal: FloatArray): Rotation =
        yawRotation(normal[0], normal[2])


    fun compressDistance(meters: Double): Float {
        val t = (meters / ArGeoConfig.MAX_DISTANCE_METERS).coerceIn(0.0, 1.0)
        return (ln(1.0 + t * ArGeoConfig.LOG_RANGE) /
                ln(ArGeoConfig.LOG_BASE) * ArGeoConfig.AR_RADIUS).toFloat()
    }

    fun calculateScale(meters: Double): Scale {
        val factor = (1.0 - meters / ArGeoConfig.MAX_DISTANCE_METERS)
            .coerceIn(ArGeoConfig.MIN_SCALE_FACTOR, 1.0).toFloat()
        val s = factor * ArGeoConfig.BASE_SCALE
        return Scale(s, s, s)
    }
}