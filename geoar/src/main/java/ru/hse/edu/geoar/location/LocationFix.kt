package ru.hse.edu.geoar.location

import ru.hse.locallense.common.entities.LocationData

data class LocationFix(
    val locationData: LocationData,
    val accuracy: Float,
    val timestamp: Long,
)
