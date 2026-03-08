package ru.hse.edu.geoar.ar

import ru.hse.locallense.common.entities.LocationData

data class ArTapResult(
    val locationData: LocationData,
    val isWall: Boolean,
)