package ru.hse.edu.geoar.math

import com.google.ar.core.Pose
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.node.Node
import ru.hse.edu.geoar.ar.ArGeoObject
import ru.hse.edu.geoar.location.LocationData
import ru.hse.edu.geoar.math.Dimens.EARTH_RADIUS_METERS
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GeoMath {

    fun distanceMeters(from: LocationData, to: ArGeoObject): Double =
        haversine(from.latitude, from.longitude, to.latitude, to.longitude)

    fun distanceMeters(cameraPose: Pose, node: Node): Double {
        val target = node.worldPosition
        val dx = target.x - cameraPose.tx()
        val dz = target.z - cameraPose.tz()
        return sqrt(dx * dx + dz * dz).toDouble()
    }

    fun bearingDegrees(from: LocationData, to: ArGeoObject): Double {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLon = Math.toRadians(to.longitude - from.longitude)
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) -
                sin(lat1) * cos(lat2) * cos(dLon)
        return Math.toDegrees(atan2(y, x)).mod(360.0)
    }

    fun relativeBearingRadians(
        headingDegrees: Float,
        from: LocationData,
        to: ArGeoObject
    ): Double {
        val bearing = bearingDegrees(from, to)
        val diff = (bearing - headingDegrees + 540.0).mod(360.0) - 180.0
        return Math.toRadians(diff)
    }

    fun relativeBearing(cameraPose: Pose, node: Node): Double {
        val target = node.worldPosition
        val forward = FloatArray(3)
        cameraPose.getTransformedAxis(2, -1f, forward, 0)

        val fwdX = forward[0]
        val fwdZ = forward[2]

        val dirX = target.x - cameraPose.tx()
        val dirZ = target.z - cameraPose.tz()

        val dot = fwdX * dirX + fwdZ * dirZ
        val cross = fwdX * dirZ - fwdZ * dirX

        return Math.toDegrees(atan2(cross, dot).toDouble())
    }
    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)
        val a = sin(dLat / 2).pow(2) + cos(rLat1) * cos(rLat2) * sin(dLon / 2).pow(2)
        return EARTH_RADIUS_METERS * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}