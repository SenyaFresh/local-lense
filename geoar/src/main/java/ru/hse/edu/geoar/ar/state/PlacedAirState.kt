package ru.hse.edu.geoar.ar.state

import com.google.ar.core.Pose
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.Node
import ru.hse.edu.geoar.ar.ArGeoConfig
import ru.hse.edu.geoar.ar.ArGeoWallFinder
import ru.hse.edu.geoar.math.ArMath
import ru.hse.edu.geoar.math.Direction2D
import ru.hse.edu.geoar.location.LocationData

class PlacedAirState(
    private val fixedPosition: Position,
    private var fixedRotation: Rotation,
    private val placedUserLocation: LocationData,
    private var lastWallRecheckTime: Long = System.currentTimeMillis()
) : ArPlacementState {

    override fun update(params: PlacementParams): ArPlacementState {
        if (placedUserLocation != params.userLocation && params.distance > ArGeoConfig.AR_RADIUS) {
            return SearchingState
        }

        val wallState = tryRecheckWall(params)
        if (wallState != null) return wallState

        applyBillboardRotation(params.cameraPose, params.arGeoObject.node)
        params.arGeoObject.node.isVisible = true

        return this
    }

    private fun tryRecheckWall(params: PlacementParams): AttachedWallState? {
        val now = System.currentTimeMillis()
        if (now - lastWallRecheckTime <= ArGeoConfig.WALL_RECHECK_INTERVAL_MS) return null
        lastWallRecheckTime = now

        val wallHit = ArGeoWallFinder.searchAroundPosition(
            params.frame, params.cameraPose, fixedPosition
        ) ?: return null

        return AttachedWallState.create(wallHit, params)
    }

    private fun applyBillboardRotation(cameraPose: Pose, node: Node) {
        val dx = cameraPose.tx() - fixedPosition.x
        val dz = cameraPose.tz() - fixedPosition.z
        fixedRotation = ArMath.yawRotation(dx, dz)
        node.worldRotation = fixedRotation
    }

    companion object {

        fun create(
            params: PlacementParams,
            direction: Direction2D
        ): PlacedAirState {
            val position = ArMath.airPosition(
                params.cameraPose, direction, params.distance
            )
            val rotation = ArMath.yawRotation(direction.x, direction.z)

            val node = params.arGeoObject.node
            node.worldPosition = position
            node.worldRotation = rotation
            node.isVisible = true

            return PlacedAirState(
                fixedPosition = position,
                fixedRotation = rotation,
                placedUserLocation = params.userLocation
            )
        }
    }
}