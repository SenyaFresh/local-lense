package ru.hse.edu.geoar.math

import android.util.Log
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

    fun bearingDegrees(from: LocationData, to: ArGeoObject): Double {
        val latitude1 = Math.toRadians(from.latitude)
        val latitude2 = Math.toRadians(to.latitude)
        val deltaLongitude = Math.toRadians(to.longitude - from.longitude)

        val y = sin(deltaLongitude) * cos(latitude2)
        val x = cos(latitude1) * sin(latitude2) -
                sin(latitude1) * cos(latitude2) * cos(deltaLongitude)

        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    fun relativeBearingRadians(
        headingDegrees: Float,
        from: LocationData,
        to: ArGeoObject
    ): Double {
        val currentBearingDegrees = bearingDegrees(from, to)
        val differenceDegrees = (currentBearingDegrees - headingDegrees + 540.0).mod(360.0) - 180.0
        return Math.toRadians(differenceDegrees)
    }

    fun haversine(
        latitude1: Double, longitude1: Double,
        latitude2: Double, longitude2: Double
    ): Double {
        val deltaLatitude = Math.toRadians(latitude2 - latitude1)
        val deltaLongitude = Math.toRadians(longitude2 - longitude1)
        val radiansLatitude1 = Math.toRadians(latitude1)
        val radiansLatitude2 = Math.toRadians(latitude2)

        val haversineValue = sin(deltaLatitude / 2).pow(2) +
                cos(radiansLatitude1) * cos(radiansLatitude2) * sin(deltaLongitude / 2).pow(2)

        return EARTH_RADIUS_METERS * 2 * atan2(sqrt(haversineValue), sqrt(1 - haversineValue))
    }
}