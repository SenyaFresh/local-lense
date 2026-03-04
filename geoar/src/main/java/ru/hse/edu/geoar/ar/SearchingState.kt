package ru.hse.edu.geoar.ar

import ru.hse.edu.geoar.geo.GeoMath

object SearchingState : ArPlacementState {

    override fun update(params: PlacementParams): ArPlacementState {
        val bearingRad = GeoMath.relativeBearingRadians(
            params.userHeading,
            params.userLocation,
            params.geoObject
        )
        val direction = ArMath.worldDirection(params.cameraPose, bearingRad)

        val wallHit = params.wallFinder.raycastWall(
            params.frame,
            params.cameraPose,
            direction.x,
            direction.z
        )

        return if (wallHit != null) {
            AttachedWallState.create(wallHit, params)
        } else {
            PlacedAirState.create(params, direction)
        }
    }
}