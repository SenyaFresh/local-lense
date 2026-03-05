package ru.hse.edu.geoar.ar.state

import com.google.ar.core.Pose
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.Node
import ru.hse.edu.geoar.ar.ArGeoConfig
import ru.hse.edu.geoar.ar.ArGeoWallFinder
import ru.hse.edu.geoar.location.LocationData
import ru.hse.edu.geoar.math.ArMath
import ru.hse.edu.geoar.math.Direction2D

class PlacedAirState(
    private val fixedPosition: Position,
    private var fixedRotation: Rotation,
    private val placedUserLocation: LocationData,
    private var lastWallRecheckTime: Long = System.currentTimeMillis()
) : ArPlacementState {

    override fun update(parameters: PlacementParameters): ArPlacementState {
        if (placedUserLocation != parameters.userLocation && parameters.distance > ArGeoConfig.AR_RADIUS) {
            return SearchingState
        }

        if (parameters.arGeoObject.isWallAnchor) {
            val wallState = tryRecheckWall(parameters)
            if (wallState != null) return wallState
        }

        applyBillboardRotation(parameters.cameraPose, parameters.arGeoObject.node)
        parameters.arGeoObject.node.isVisible = true

        return this
    }

    private fun tryRecheckWall(parameters: PlacementParameters): AttachedWallState? {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastWallRecheckTime <= ArGeoConfig.WALL_RECHECK_INTERVAL_MS) return null
        lastWallRecheckTime = currentTime

        val wallHitResult = ArGeoWallFinder.searchAroundPosition(
            parameters.frame, parameters.cameraPose, fixedPosition
        ) ?: return null

        return AttachedWallState.create(wallHitResult, parameters)
    }

    private fun applyBillboardRotation(cameraPose: Pose, node: Node) {
        val deltaX = cameraPose.tx() - fixedPosition.x
        val deltaZ = cameraPose.tz() - fixedPosition.z
        fixedRotation = ArMath.yawRotation(deltaX, deltaZ)
        node.worldRotation = fixedRotation
    }

    companion object {

        fun create(
            parameters: PlacementParameters,
            direction: Direction2D
        ): PlacedAirState {
            val position = ArMath.airPosition(
                parameters.cameraPose, direction, parameters.distance
            )
            val rotation = ArMath.yawRotation(direction.x, direction.z)

            val node = parameters.arGeoObject.node
            node.worldPosition = position
            node.worldRotation = rotation
            node.isVisible = true

            return PlacedAirState(
                fixedPosition = position,
                fixedRotation = rotation,
                placedUserLocation = parameters.userLocation
            )
        }
    }
}