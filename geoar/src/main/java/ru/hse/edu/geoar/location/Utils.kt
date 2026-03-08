package ru.hse.edu.geoar.location

import android.location.Location
import ru.hse.locallense.common.entities.LocationData

fun Location.toLocationFix() = LocationFix(
    locationData = LocationData(
        latitude = latitude,
        longitude = longitude,
        altitude = altitude
    ),
    accuracy = accuracy,
    timestamp = time,
)