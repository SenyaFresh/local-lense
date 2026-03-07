package ru.hse.edu.geoar.ar.state

import ru.hse.edu.geoar.ar.ArGeoWallFinder
import ru.hse.edu.geoar.math.ArMath
import ru.hse.edu.geoar.math.GeoMath.relativeBearingRadians

object StateUpdater {

    fun update(state: ArPlacementState, parameters: PlacementParameters): ArPlacementState {
        if (state.isValid(parameters)) {
            state.update(parameters)
            return state
        }
        val relativeBearingRadians = relativeBearingRadians(
            parameters.initialCameraHeading,
            parameters.userLocation,
            parameters.arGeoObject
        )
        return when (state) {
            is InitialState, is AttachedWallState -> {
                state.release()
                tryFindWall(parameters, relativeBearingRadians) ?: PlacedAirState.create(
                    parameters,
                    relativeBearingRadians
                )
            }

            is PlacedAirState -> {
                val wallState = tryFindWall(parameters, relativeBearingRadians)
                if (wallState != null) {
                    state.release()
                    wallState
                } else {
                    state.update(parameters)
                    state
                }
            }
        }
    }

    private fun tryFindWall(
        parameters: PlacementParameters,
        relativeBearingRadians: Double
    ): AttachedWallState? {
        if (!parameters.arGeoObject.isWallAnchor) return null

        val altitudeDifference = parameters.arGeoObject.altitude - parameters.userLocation.altitude
        val objectPosition = ArMath.airPosition(
            cameraPose = parameters.cameraPose,
            relativeBearingRadians = relativeBearingRadians,
            realDistanceMeters = parameters.distance,
            altitudeDifference = altitudeDifference
        )
        val wallHitResult = ArGeoWallFinder.searchAroundPosition(
            parameters.frame, parameters.cameraPose, objectPosition
        ) ?: return null
        return AttachedWallState.create(wallHitResult, parameters)
    }
}