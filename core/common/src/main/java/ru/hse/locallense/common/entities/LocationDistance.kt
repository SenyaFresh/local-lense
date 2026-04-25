package ru.hse.locallense.common.entities

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0

fun LocationData.distanceMetersTo(other: LocationData): Double {
    val deltaLatitudeRadians = Math.toRadians(other.latitude - latitude)
    val deltaLongitudeRadians = Math.toRadians(other.longitude - longitude)
    val latitude1Radians = Math.toRadians(latitude)
    val latitude2Radians = Math.toRadians(other.latitude)

    val haversine = sin(deltaLatitudeRadians / 2).pow(2) +
            cos(latitude1Radians) * cos(latitude2Radians) *
            sin(deltaLongitudeRadians / 2).pow(2)

    val centralAngle = 2 * atan2(sqrt(haversine), sqrt(1 - haversine))
    return EARTH_RADIUS_METERS * centralAngle
}
