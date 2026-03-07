package ru.hse.edu.geoar.ar.state

import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import ru.hse.edu.geoar.ar.ArGeoConfig
import ru.hse.edu.geoar.math.ArMath

class AttachedWallState(
    private val anchor: Anchor,
    private val plane: Plane,
) : ArPlacementState {

    override fun isValid(parameters: PlacementParameters): Boolean {
        return anchor.trackingState == TrackingState.TRACKING && plane.trackingState == TrackingState.TRACKING
    }

    override fun update(parameters: PlacementParameters) = Unit

    override fun release() = anchor.detach()

    companion object {
        fun create(hitResult: HitResult, parameters: PlacementParameters): AttachedWallState {
            val anchor = hitResult.createAnchor()
            val plane = hitResult.trackable as Plane

            val normal = FloatArray(3)
            plane.centerPose.getTransformedAxis(1, 1f, normal, 0)

            val node = parameters.arGeoObject.node
            node.worldPosition = ArMath.wallPosition(
                anchor.pose, normal, ArGeoConfig.WALL_OFFSET
            )
            node.worldRotation = ArMath.wallRotation(normal)

            return AttachedWallState(anchor, plane)
        }
    }
}