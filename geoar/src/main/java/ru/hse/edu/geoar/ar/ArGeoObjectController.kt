package ru.hse.edu.geoar.ar

import com.google.ar.core.Frame
import com.google.ar.core.Pose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.hse.edu.geoar.ar.state.ArPlacementState
import ru.hse.edu.geoar.ar.state.PlacementParameters
import ru.hse.edu.geoar.ar.state.InitialState
import ru.hse.edu.geoar.ar.state.StateUpdater
import ru.hse.edu.geoar.math.GeoMath
import ru.hse.edu.geoar.location.LocationData
import ru.hse.edu.geoar.math.ArMath
import ru.hse.edu.geoar.math.GeoMath.distanceMeters
import kotlin.math.roundToInt

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
        initialPose: Pose,
    ) {
        val distance = distanceMeters(userLocation, arGeoObject)
        arGeoObject.node.scale = ArMath.calculateScale(distance)
        val parameters = PlacementParameters(
            arGeoObject = arGeoObject,
            userLocation = userLocation,
            userHeading = userHeading,
            frame = frame,
            cameraPose = cameraPose,
            distance = distance,
            initialCameraHeading = initialCameraHeading,
            initialPose = initialPose,
        )
        state = StateUpdater.update(state, parameters)
        arGeoObject.node.isVisible = true

        val bearing = Math.toDegrees(
            GeoMath.relativeBearingRadians(
                headingDegrees = initialCameraHeading,
                from = userLocation,
                to = arGeoObject
            )
        )
        _info.value = ArGeoObjectPlacementResult(
            distanceMeters = (distance * 100).roundToInt() / 100.0,
            bearing = ((userHeading - bearing - initialCameraHeading + 360) % 360 * 100).roundToInt() / 100.0,
            state = state,
        )
    }

    fun detach() {
        state.release()
        state = InitialState
    }
}