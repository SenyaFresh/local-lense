package ru.hse.edu.locallense.ar

import androidx.compose.runtime.Composable
import ru.hse.edu.geoar.ar.ArGeoObjectPlacementResult
import ru.hse.locallense.common.entities.LocationData

class ArGeoMarker(
    val locationData: LocationData,
    val isWallAnchor: Boolean,
)