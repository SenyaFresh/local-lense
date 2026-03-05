package ru.hse.edu.geoar.ar.state

import com.google.ar.core.Frame
import com.google.ar.core.Pose
import ru.hse.edu.geoar.ar.ArGeoObject
import ru.hse.edu.geoar.location.LocationData

data class PlacementParams(
    val arGeoObject: ArGeoObject,
    val userLocation: LocationData,
    val userHeading: Float,
    val frame: Frame,
    val cameraPose: Pose,
    val distance: Double
)

sealed interface ArPlacementState {
    fun update(params: PlacementParams): ArPlacementState
    fun release() {}
}