package ru.hse.edu.geoar.ar

import com.google.ar.core.Frame
import com.google.ar.core.Pose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.hse.edu.geoar.ar.state.ArPlacementState
import ru.hse.edu.geoar.ar.state.AttachedWallState
import ru.hse.edu.geoar.ar.state.InitialState
import ru.hse.edu.geoar.ar.state.PlacementParameters
import ru.hse.edu.geoar.ar.state.StateUpdater
import ru.hse.edu.geoar.location.LocationData
import ru.hse.edu.geoar.math.ArMath
import ru.hse.edu.geoar.math.GeoMath
import ru.hse.edu.geoar.math.GeoMath.distanceMeters
import ru.hse.edu.geoar.math.round

class ArGeoObjectController(val arGeoObject: ArGeoObject) {

    private var state: ArPlacementState = InitialState
    private val _info = MutableStateFlow<ArGeoObjectPlacementResult?>(null)
    val info: StateFlow<ArGeoObjectPlacementResult?> = _info.asStateFlow()

    fun update(
        userLocation: LocationData,
        userHeading: Float,
        frame: Frame,
        cameraPose: Pose,
        initialCameraHeading: Float,
    ) {
        val horizontalDistance = distanceMeters(userLocation, arGeoObject)
        val altitudeDifference = arGeoObject.altitude - userLocation.altitude
        val distance3D = ArMath.distance3D(horizontalDistance, altitudeDifference)
        arGeoObject.node.scale = ArMath.calculateScale(distance3D)
        val parameters = PlacementParameters(
            arGeoObject = arGeoObject,
            userLocation = userLocation,
            userHeading = userHeading,
            frame = frame,
            cameraPose = cameraPose,
            distance = horizontalDistance,
            initialCameraHeading = initialCameraHeading,
        )
        state = StateUpdater.update(state, parameters)
        arGeoObject.node.isVisible = true
        _info.value = buildInfo(parameters)
    }

    private fun buildInfo(parameters: PlacementParameters): ArGeoObjectPlacementResult {
        val relativeBearing = GeoMath.relativeBearingDegrees(
            cameraPose = parameters.cameraPose,
            node = parameters.arGeoObject.node
        )
        val distance = if (state is AttachedWallState) {
            distanceMeters(
                cameraPose = parameters.cameraPose,
                node = parameters.arGeoObject.node
            )
        } else {
            parameters.distance
        }
        return ArGeoObjectPlacementResult(
            distanceMeters = distance.round(2),
            bearing = relativeBearing.round(2),
            state = state,
        )
    }

    fun detach() {
        state.release()
        state = InitialState
    }
}