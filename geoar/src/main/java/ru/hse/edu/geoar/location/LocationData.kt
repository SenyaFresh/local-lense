package ru.hse.edu.geoar.location

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long,
)
