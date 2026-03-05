package ru.hse.edu.geoar.ar.state

import ru.hse.edu.geoar.ar.ArGeoWallFinder
import ru.hse.edu.geoar.math.ArMath
import ru.hse.edu.geoar.math.GeoMath

object SearchingState : ArPlacementState {

    override fun update(parameters: PlacementParameters): ArPlacementState {
        val bearingRadians = GeoMath.relativeBearingRadians(
            parameters.userHeading,
            parameters.userLocation,
            parameters.arGeoObject
        )
        val direction = ArMath.worldDirection(parameters.cameraPose, bearingRadians)

        if (!parameters.arGeoObject.isWallAnchor) {
            return PlacedAirState.create(parameters, direction)
        }

        val objectPosition = ArMath.airPosition(parameters.cameraPose, direction, parameters.distance)
        val wallHitResult = ArGeoWallFinder.searchAroundPosition(
            parameters.frame, parameters.cameraPose, objectPosition
        )

        return if (wallHitResult != null) {
            AttachedWallState.create(wallHitResult, parameters)
        } else {
            PlacedAirState.create(parameters, direction)
        }
    }
}