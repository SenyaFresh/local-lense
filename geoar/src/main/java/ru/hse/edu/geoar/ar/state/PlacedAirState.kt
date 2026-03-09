package ru.hse.edu.geoar.ar.state

import android.util.Log
import ru.hse.edu.geoar.ar.ArGeoConfig
import ru.hse.edu.geoar.math.ArMath
import ru.hse.edu.geoar.math.GeoMath.relativeBearingRadians

class PlacedAirState : ArPlacementState() {

    override fun isValid(parameters: PlacementParameters) =
        parameters.distance > ArGeoConfig.AR_RADIUS

    private var isInitialized = false

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
            altitudeDifference = parameters.arGeoObject.locationData.altitude - parameters.userLocation.altitude
        )

        if (!isInitialized || parameters.distance > ArGeoConfig.AR_RADIUS) {
            node.worldPosition = newPosition
            applyBillboardRotation(parameters.cameraPose, node)
            return
        }

        applyBillboardRotation(parameters.cameraPose, node)
    }

    companion object {
        fun create(
            parameters: PlacementParameters,
        ): PlacedAirState {
            val state = PlacedAirState()
            state.update(parameters)
            state.isInitialized = true
            return state
        }
    }
}