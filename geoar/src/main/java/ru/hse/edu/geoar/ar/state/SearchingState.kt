package ru.hse.edu.geoar.ar.state

import ru.hse.edu.geoar.ar.ArGeoWallFinder
import ru.hse.edu.geoar.ar.ArMath
import ru.hse.edu.geoar.geo.GeoMath

object SearchingState : ArPlacementState {

    override fun update(params: PlacementParams): ArPlacementState {
        val bearingRad = GeoMath.relativeBearingRadians(
            params.userHeading,
            params.userLocation,
            params.geoObject
        )
        val direction = ArMath.worldDirection(params.cameraPose, bearingRad)
        val objectPosition = ArMath.airPosition(params.cameraPose, direction, params.distance)

        val wallHit = ArGeoWallFinder.searchAroundPosition(
            params.frame, params.cameraPose, objectPosition
        )

        return if (wallHit != null) {
            AttachedWallState.create(wallHit, params)
        } else {
            PlacedAirState.create(params, direction)
        }
    }
}