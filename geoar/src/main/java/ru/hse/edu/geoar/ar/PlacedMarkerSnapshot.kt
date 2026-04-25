package ru.hse.edu.geoar.ar

import ru.hse.locallense.common.entities.LocationData

data class PlacedMarkerSnapshot(
    val id: Long?,
    val locationData: LocationData,
    val distanceMeters: Double,
    val screenBearingDegrees: Double,
    val altitudeDifferenceMeters: Double,
)
