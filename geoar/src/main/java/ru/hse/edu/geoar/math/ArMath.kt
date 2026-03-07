package ru.hse.edu.geoar.math

import com.google.ar.core.Pose
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import ru.hse.edu.geoar.ar.ArGeoConfig
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin

object ArMath {

    fun yawDegrees(deltaX: Float, deltaZ: Float): Float =
        Math.toDegrees(atan2(deltaX.toDouble(), deltaZ.toDouble())).toFloat()

    fun yawRotation(deltaX: Float, deltaZ: Float): Rotation =
        Rotation(0f, yawDegrees(deltaX, deltaZ), 0f)

    fun yawRotation(relativeBearingRadians: Double): Rotation =
        Rotation(0f, Math.toDegrees(relativeBearingRadians).toFloat(), 0f)

    fun airPosition(
        cameraPose: Pose,
        initialPose: Pose,
        relativeBearingRadians: Double,
        realDistanceMeters: Double
    ): Position {
        val arDistance = compressDistance(realDistanceMeters)
        val dirX = sin(relativeBearingRadians).toFloat()
        val dirZ = -cos(relativeBearingRadians).toFloat()
        return Position(
            cameraPose.tx() + dirX * arDistance,
            initialPose.ty(),
            cameraPose.tz() + dirZ * arDistance
        )
    }

    fun wallPosition(
        initialPose: Pose,
        anchorPose: Pose,
        normal: FloatArray,
        offset: Float
    ): Position = Position(
        anchorPose.tx() + normal[0] * offset,
        initialPose.ty(),
        anchorPose.tz() + normal[2] * offset
    )

    fun wallRotation(normal: FloatArray): Rotation =
        yawRotation(normal[0], normal[2])


    fun compressDistance(meters: Double): Float {
        if (meters <= ArGeoConfig.AR_RADIUS) {
            return meters.toFloat()
        }

        val excess = meters - ArGeoConfig.AR_RADIUS
        val k = ArGeoConfig.AR_RADIUS
        val compressed = ArGeoConfig.AR_RADIUS + k * ln(1.0 + excess / k)
        return compressed.toFloat()
    }

    fun calculateScale(meters: Double): Scale {
        if (meters <= ArGeoConfig.AR_RADIUS) {
            val s = ArGeoConfig.BASE_SCALE
            return Scale(s, s, s)
        }

        val t = ((meters - ArGeoConfig.AR_RADIUS) /
                (ArGeoConfig.MAX_DISTANCE_METERS - ArGeoConfig.AR_RADIUS))
            .coerceIn(0.0, 1.0)

        val smooth = t * t * (3.0 - 2.0 * t)
        val factor = (1.0 - smooth * (1.0 - ArGeoConfig.MIN_SCALE_FACTOR)).toFloat()
        val s = factor * ArGeoConfig.BASE_SCALE
        return Scale(s, s, s)
    }
}