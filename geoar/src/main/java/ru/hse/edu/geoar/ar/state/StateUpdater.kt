package ru.hse.edu.geoar.ar.state

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

            else -> {
                state.release()
                tryFindWall(parameters, relativeBearingRadians) ?: PlacedAirState.create(
                    parameters,
                )
            }
        }
    }

    private fun tryFindWall(
        parameters: PlacementParameters,
        relativeBearingRadians: Double
    ): AttachedWallState? {
        if (!parameters.arGeoObject.isWallAnchor) return null

        val objectPosition = ArMath.airPosition(
            cameraPose = parameters.cameraPose,
            relativeBearingRadians = relativeBearingRadians,
            realDistanceMeters = parameters.distance,
            altitudeDifference = parameters.arGeoObject.altitude - parameters.userLocation.altitude
        )

        val wallHitResult = ArGeoWallFinder.searchAroundPosition(
            frame = parameters.frame,
            cameraPose = parameters.cameraPose,
            objectPosition = objectPosition
        ) ?: return null

        return AttachedWallState.create(wallHitResult, parameters)
    }
}