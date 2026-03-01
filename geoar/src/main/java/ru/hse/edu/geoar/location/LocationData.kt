package ru.hse.edu.geoar.location

/**
 * Class representing location data.
 * @property latitude The latitude of the location.
 * @property longitude The longitude of the location.
 * @property accuracy The accuracy of the location in meters.
 * @property speed The speed of the location in meters per second.
 * @property bearing The direction of the location in degrees.
 * @property altitude The height of the location above the sea level in meters.
 * @property timestamp The timestamp of the location in milliseconds from System.currentTimeMillis().
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val speed: Float,
    val bearing: Float,
    val altitude: Double,
    val timestamp: Long,
)
