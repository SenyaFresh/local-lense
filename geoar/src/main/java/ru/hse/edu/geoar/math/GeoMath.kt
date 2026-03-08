package ru.hse.edu.geoar.math

import com.google.ar.core.Pose
import io.github.sceneview.node.Node
import ru.hse.edu.geoar.ar.ArGeoObject
import ru.hse.edu.geoar.math.Dimens.EARTH_RADIUS_METERS
import ru.hse.locallense.common.entities.LocationData
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GeoMath {

    fun distanceMeters(from: LocationData, to: ArGeoObject): Double {
        val to = to.locationData
        return haversine(
            latitude1 = from.latitude, longitude1 = from.longitude,
            latitude2 = to.latitude, longitude2 = to.longitude
        )
    }

    fun distanceMeters(cameraPose: Pose, node: Node): Double {
        val targetPosition = node.worldPosition
        return horizontalDistance(
            x1 = cameraPose.tx().toDouble(), z1 = cameraPose.tz().toDouble(),
            x2 = targetPosition.x.toDouble(), z2 = targetPosition.z.toDouble()
        )
    }

    fun relativeBearingRadians(
        headingDegrees: Float,
        from: LocationData,
        to: ArGeoObject
    ): Double {
        val to = to.locationData
        val bearingToTarget = calculateBearingDegrees(
            latitude1 = from.latitude, longitude1 = from.longitude,
            latitude2 = to.latitude, longitude2 = to.longitude
        )
        val normalizedDifference = normalizeToPlusMinus180(bearingToTarget - headingDegrees)
        return Math.toRadians(normalizedDifference)
    }

    fun relativeBearingDegrees(cameraPose: Pose, node: Node): Double {
        val targetPosition = node.worldPosition
        val cameraForwardAxis = FloatArray(3)
        cameraPose.getTransformedAxis(2, -1f, cameraForwardAxis, 0)

        val directionToTargetX = targetPosition.x - cameraPose.tx()
        val directionToTargetZ = targetPosition.z - cameraPose.tz()

        return signedAngleBetweenVectors2D(
            cameraForwardAxis[0].toDouble(), cameraForwardAxis[2].toDouble(),
            directionToTargetX.toDouble(), directionToTargetZ.toDouble()
        )
    }

    private fun haversine(
        latitude1: Double, longitude1: Double,
        latitude2: Double, longitude2: Double
    ): Double {
        val deltaLatitudeRadians = Math.toRadians(latitude2 - latitude1)
        val deltaLongitudeRadians = Math.toRadians(longitude2 - longitude1)
        val latitude1Radians = Math.toRadians(latitude1)
        val latitude2Radians = Math.toRadians(latitude2)

        val haversineCoefficient = sin(deltaLatitudeRadians / 2).pow(2) +
                cos(latitude1Radians) * cos(latitude2Radians) *
                sin(deltaLongitudeRadians / 2).pow(2)

        val centralAngle = 2 * atan2(sqrt(haversineCoefficient), sqrt(1 - haversineCoefficient))

        return EARTH_RADIUS_METERS * centralAngle
    }

    private fun calculateBearingDegrees(
        latitude1: Double, longitude1: Double,
        latitude2: Double, longitude2: Double
    ): Double {
        val latitude1Radians = Math.toRadians(latitude1)
        val latitude2Radians = Math.toRadians(latitude2)
        val deltaLongitudeRadians = Math.toRadians(longitude2 - longitude1)

        val eastwardProjection = sin(deltaLongitudeRadians) * cos(latitude2Radians)
        val northwardProjection = cos(latitude1Radians) * sin(latitude2Radians) -
                sin(latitude1Radians) * cos(latitude2Radians) * cos(deltaLongitudeRadians)

        return Math.toDegrees(atan2(eastwardProjection, northwardProjection)).mod(360.0)
    }

    private fun horizontalDistance(
        x1: Double, z1: Double,
        x2: Double, z2: Double
    ): Double {
        val deltaX = x2 - x1
        val deltaZ = z2 - z1
        return sqrt(deltaX * deltaX + deltaZ * deltaZ)
    }

    private fun signedAngleBetweenVectors2D(
        firstX: Double, firstZ: Double,
        secondX: Double, secondZ: Double
    ): Double {
        val dotProduct = firstX * secondX + firstZ * secondZ
        val crossProduct = firstX * secondZ - firstZ * secondX
        return Math.toDegrees(atan2(crossProduct, dotProduct))
    }

    private fun normalizeToPlusMinus180(degrees: Double): Double =
        (degrees + 540.0).mod(360.0) - 180.0
}