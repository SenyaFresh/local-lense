package ru.hse.edu.geoar.ar

import ru.hse.edu.geoar.ar.state.ArPlacementState

data class ArGeoObjectPlacementResult(
    val distanceMeters: Double,
    val bearing: Double,
    val state: ArPlacementState,
)