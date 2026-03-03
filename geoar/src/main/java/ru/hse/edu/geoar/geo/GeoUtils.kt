package ru.hse.edu.geoar.geo

import io.github.sceneview.math.Scale
import ru.hse.edu.geoar.location.LocationData
import ru.hse.edu.geoar.main.ArGeoConfig
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GeoUtils {

    private const val EARTH_RADIUS_M = 6_371_000.0

    fun distanceMeters(from: LocationData, to: GeoObject): Double {
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLon = Math.toRadians(to.longitude - from.longitude)
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)

        val a = sin(dLat / 2).pow(2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)

        return EARTH_RADIUS_M * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    fun bearingDegrees(from: LocationData, to: GeoObject): Double {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLon = Math.toRadians(to.longitude - from.longitude)

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) -
                sin(lat1) * cos(lat2) * cos(dLon)

        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    fun relativeBearing(currentHeading: Float, from: LocationData, to: GeoObject): Double {
        val bearing = bearingDegrees(from, to)
        val diff = bearing - currentHeading

        return (diff + 540.0).mod(360.0) - 180.0
    }

    fun compressDistance(meters: Double): Double {
        val t = (meters / ArGeoConfig.MAX_DISTANCE_METERS).coerceIn(0.0, 1.0)
        return ln(1.0 + t * ArGeoConfig.LOG_RANGE) /
                ln(ArGeoConfig.LOG_BASE) * ArGeoConfig.AR_RADIUS
    }

    fun calculateScale(meters: Double): Scale {
        val factor = (1.0 - meters / ArGeoConfig.MAX_DISTANCE_METERS)
            .coerceIn(ArGeoConfig.MIN_SCALE_FACTOR, 1.0).toFloat()
        val s = factor * ArGeoConfig.BASE_SCALE
        return Scale(s, s, s)
    }
}