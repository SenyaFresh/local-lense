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
import kotlin.math.sqrt

object ArMath {

    fun billboardRotation(deltaX: Float, deltaY: Float, deltaZ: Float): Rotation {
        val yaw = Math.toDegrees(atan2(deltaX, deltaZ).toDouble()).toFloat()
        val horizontalDist = sqrt(deltaX * deltaX + deltaZ * deltaZ)
        val pitch = -Math.toDegrees(atan2(deltaY, horizontalDist).toDouble()).toFloat()
        return Rotation(pitch, yaw, 0f)
    }

    fun airPosition(
        cameraPose: Pose,
        relativeBearingRadians: Double,
        realDistanceMeters: Double,
        altitudeDifference: Double
    ): Position {
        val compressedDistance = compressDistance(realDistanceMeters)
        val verticalOffset = proportionalVerticalOffset(
            realDistanceMeters, altitudeDifference, compressedDistance
        )
        return offsetPositionByBearing(
            cameraPose.tx(), cameraPose.ty(), cameraPose.tz(),
            relativeBearingRadians, compressedDistance, verticalOffset
        )
    }

    fun wallPosition(
        anchorPose: Pose,
        normal: FloatArray,
        offset: Float
    ): Position = offsetPositionByNormal(
        anchorPose.tx(), anchorPose.ty(), anchorPose.tz(),
        normal[0], normal[2], offset
    )

    fun compressDistance(meters: Double): Float {
        val arRadius = ArGeoConfig.AR_RADIUS
        if (meters <= arRadius) return meters.toFloat()
        val logarithmicOverflow = arRadius * ln(1.0 + (meters - arRadius) / arRadius)
        return (arRadius + logarithmicOverflow).toFloat()
    }

    fun distance3D(horizontalMeters: Double, altitudeDifference: Double): Double =
        sqrt(horizontalMeters * horizontalMeters + altitudeDifference * altitudeDifference)

    fun calculateScale(meters: Double): Scale {
        if (meters <= ArGeoConfig.AR_RADIUS) {
            return uniformScale(ArGeoConfig.BASE_SCALE)
        }

        val normalizedDistance = ((meters - ArGeoConfig.AR_RADIUS) /
                (ArGeoConfig.MAX_DISTANCE_METERS - ArGeoConfig.AR_RADIUS))
            .coerceIn(0.0, 1.0)

        val smoothedFraction = smoothStep(normalizedDistance)
        val scaleFactor = (1.0 - smoothedFraction * (1.0 - ArGeoConfig.MIN_SCALE_FACTOR)).toFloat()
        return uniformScale(scaleFactor * ArGeoConfig.BASE_SCALE)
    }

    private fun offsetPositionByBearing(
        originX: Float, originY: Float, originZ: Float,
        bearingRadians: Double,
        horizontalDistance: Float,
        verticalOffset: Float
    ): Position = Position(
        originX + sin(bearingRadians).toFloat() * horizontalDistance,
        originY + verticalOffset,
        originZ - cos(bearingRadians).toFloat() * horizontalDistance
    )

    private fun offsetPositionByNormal(
        originX: Float, originY: Float, originZ: Float,
        normalX: Float, normalZ: Float,
        offset: Float
    ): Position = Position(
        originX + normalX * offset,
        originY,
        originZ + normalZ * offset
    )

    private fun proportionalVerticalOffset(
        realDistanceMeters: Double,
        altitudeDifference: Double,
        compressedDistance: Float
    ): Float = (altitudeDifference * compressedDistance / realDistanceMeters).toFloat()

    private fun smoothStep(value: Double): Double =
        value * value * (3.0 - 2.0 * value)

    private fun uniformScale(value: Float): Scale =
        Scale(value, value, value)
}