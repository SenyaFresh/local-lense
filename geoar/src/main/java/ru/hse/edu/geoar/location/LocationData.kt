package ru.hse.edu.geoar.location

/**
 * Class representing location data.
 * @property latitude The latitude of the location.
 * @property longitude The longitude of the location.
 * @property accuracy The accuracy of the location in meters.
 * @property timestamp The timestamp of the location in milliseconds from System.currentTimeMillis().
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long,
)
