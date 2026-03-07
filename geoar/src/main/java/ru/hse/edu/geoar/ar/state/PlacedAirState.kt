package ru.hse.edu.geoar.ar.state

import android.util.Log
import com.google.ar.core.Pose
import io.github.sceneview.math.Position
import io.github.sceneview.node.Node
import ru.hse.edu.geoar.ar.ArGeoConfig
import ru.hse.edu.geoar.location.LocationData
import ru.hse.edu.geoar.math.ArMath
import ru.hse.edu.geoar.math.GeoMath

class PlacedAirState(
    private var lastUserLocation: LocationData,
) : ArPlacementState {

    override fun isValid(parameters: PlacementParameters) = parameters.distance > ArGeoConfig.AR_RADIUS

    override fun update(parameters: PlacementParameters) {
        lastUserLocation = parameters.userLocation

        val node = parameters.arGeoObject.node

        val relativeBearingRadians = GeoMath.relativeBearingRadians(
            headingDegrees = parameters.initialCameraHeading,
            from = parameters.userLocation,
            to = parameters.arGeoObject
        )

        val newPosition = ArMath.airPosition(
            cameraPose = parameters.cameraPose,
            anchorPose = parameters.initialPose,
            relativeBearingRadians = relativeBearingRadians,
            realDistanceMeters = parameters.distance
        )

        node.worldPosition = newPosition

        applyBillboardRotation(parameters.cameraPose, node, newPosition)
    }

    private fun applyBillboardRotation(cameraPose: Pose, node: Node, position: Position) {
        val deltaX = cameraPose.tx() - position.x
        val deltaZ = cameraPose.tz() - position.z
        node.worldRotation = ArMath.yawRotation(deltaX, deltaZ)
    }

    companion object {
        fun create(parameters: PlacementParameters, relativeBearingRadians: Double): PlacedAirState {
            val position = ArMath.airPosition(
                anchorPose = parameters.initialPose,
                cameraPose = parameters.cameraPose,
                relativeBearingRadians = relativeBearingRadians,
                realDistanceMeters = parameters.distance
            )
            val rotation = ArMath.yawRotation(relativeBearingRadians)
            val node = parameters.arGeoObject.node
            node.worldPosition = position
            node.worldRotation = rotation
            return PlacedAirState(lastUserLocation = parameters.userLocation)
        }
    }
}