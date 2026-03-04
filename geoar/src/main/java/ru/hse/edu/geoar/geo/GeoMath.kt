package ru.hse.edu.geoar.geo

import ru.hse.edu.geoar.location.LocationData
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GeoMath {

    private const val EARTH_RADIUS_M = 6_371_000.0

    fun distanceMeters(from: LocationData, to: GeoObject): Double =
        haversine(from.latitude, from.longitude, to.latitude, to.longitude)

    fun distanceMeters(from: LocationData, to: LocationData): Double =
        haversine(from.latitude, from.longitude, to.latitude, to.longitude)

    fun bearingDegrees(from: LocationData, to: GeoObject): Double {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLon = Math.toRadians(to.longitude - from.longitude)

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) -
                sin(lat1) * cos(lat2) * cos(dLon)

        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    fun relativeBearingRadians(
        headingDegrees: Float,
        from: LocationData,
        to: GeoObject
    ): Double {
        val bearingDeg = bearingDegrees(from, to)
        val diffDeg = (bearingDeg - headingDegrees + 540.0).mod(360.0) - 180.0
        return Math.toRadians(diffDeg)
    }

    private fun haversine(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2) +
                cos(rLat1) * cos(rLat2) * sin(dLon / 2).pow(2)

        return EARTH_RADIUS_M * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}