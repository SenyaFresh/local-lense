package ru.hse.edu.geoar.ar.state

import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import ru.hse.edu.geoar.ar.ArGeoConfig
import ru.hse.edu.geoar.math.ArMath

class AttachedWallState(
    private val anchor: Anchor,
) : ArPlacementState() {

    override fun isValid(parameters: PlacementParameters): Boolean {
        return anchor.trackingState == TrackingState.TRACKING
    }

    override fun update(parameters: PlacementParameters) {
        applyBillboardRotation(parameters.cameraPose, parameters.arGeoObject.node)
    }

    override fun release() = anchor.detach()

    companion object {
        fun create(hitResult: HitResult, parameters: PlacementParameters): AttachedWallState {
            val anchor = hitResult.createAnchor()
            val plane = hitResult.trackable as Plane

            val normal = FloatArray(3)
            plane.centerPose.getTransformedAxis(1, 1f, normal, 0)
            val node = parameters.arGeoObject.node
            node.worldPosition = ArMath.wallPosition(
                anchorPose = anchor.pose,
                normal = normal,
                offset = ArGeoConfig.WALL_OFFSET
            )

            val state = AttachedWallState(anchor)
            state.update(parameters)

            return state
        }
    }
}