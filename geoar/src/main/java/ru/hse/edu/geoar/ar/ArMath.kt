package ru.hse.edu.geoar.ar

import com.google.ar.core.Pose
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

data class Direction2D(val x: Float, val z: Float)

object ArMath {

    fun worldDirection(
        cameraPose: Pose,
        relativeBearingRad: Double
    ): Direction2D {
        val fwd = FloatArray(3)
        cameraPose.getTransformedAxis(2, -1f, fwd, 0)

        val right = FloatArray(3)
        cameraPose.getTransformedAxis(0, 1f, right, 0)

        val cosA = cos(relativeBearingRad).toFloat()
        val sinA = sin(relativeBearingRad).toFloat()

        val dx = fwd[0] * cosA + right[0] * sinA
        val dz = fwd[2] * cosA + right[2] * sinA

        val len = sqrt(dx * dx + dz * dz)
        return if (len > 1e-4f) Direction2D(dx / len, dz / len)
        else Direction2D(0f, -1f)
    }

    fun horizontalDirection(
        fromX: Float, fromZ: Float,
        toX: Float, toZ: Float
    ): Direction2D? {
        val dx = toX - fromX
        val dz = toZ - fromZ
        val len = sqrt(dx * dx + dz * dz)
        return if (len > 1e-4f) Direction2D(dx / len, dz / len) else null
    }

    fun yawDegrees(dx: Float, dz: Float): Float =
        Math.toDegrees(atan2(dx.toDouble(), dz.toDouble())).toFloat()

    fun yawRotation(dx: Float, dz: Float): Rotation =
        Rotation(0f, yawDegrees(dx, dz), 0f)

    fun airPosition(
        cameraPose: Pose,
        direction: Direction2D,
        realDistanceM: Double
    ): Position {
        val arDist = compressDistance(realDistanceM)
        return Position(
            cameraPose.tx() + direction.x * arDist,
            floor(cameraPose.ty()),
            cameraPose.tz() + direction.z * arDist
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