package ru.hse.edu.geoar.math

import kotlin.math.cos

object GeoConstants {
    const val EARTH_RADIUS_METERS = 6_371_000.0

    const val STEP_LENGTH_METERS = 0.72

    fun metersPerDegreeLatitude() = 111_320.0

    fun metersPerDegreeLongitude(latitudeDegrees: Double): Double =
        metersPerDegreeLatitude() * cos(Math.toRadians(latitudeDegrees))
}
