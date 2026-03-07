package ru.hse.edu.geoar.ar.state

import ru.hse.edu.geoar.ar.ArGeoConfig
import ru.hse.edu.geoar.math.ArMath
import ru.hse.edu.geoar.math.GeoMath.relativeBearingRadians

class PlacedAirState : ArPlacementState() {

    override fun isValid(parameters: PlacementParameters) =
        parameters.distance > ArGeoConfig.AR_RADIUS

    override fun update(parameters: PlacementParameters) {
        val node = parameters.arGeoObject.node

        val relativeBearingRadians = relativeBearingRadians(
            headingDegrees = parameters.initialCameraHeading,
            from = parameters.userLocation,
            to = parameters.arGeoObject
        )

        val newPosition = ArMath.airPosition(
            cameraPose = parameters.cameraPose,
            relativeBearingRadians = relativeBearingRadians,
            realDistanceMeters = parameters.distance,
            altitudeDifference = parameters.arGeoObject.altitude - parameters.userLocation.altitude
        )

        node.worldPosition = newPosition

        applyBillboardRotation(parameters.cameraPose, node)
    }

    companion object {
        fun create(
            parameters: PlacementParameters,
        ): PlacedAirState {
            val state = PlacedAirState()
            state.update(parameters)
            return state
        }
    }
}