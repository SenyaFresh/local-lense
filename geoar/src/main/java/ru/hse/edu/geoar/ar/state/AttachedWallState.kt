package ru.hse.edu.geoar.ar.state

import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import io.github.sceneview.node.Node
import ru.hse.edu.geoar.ar.ArGeoConfig
import ru.hse.edu.geoar.math.ArMath

class AttachedWallState(
    private val anchor: Anchor,
    private val plane: Plane,
) : ArPlacementState {

    override fun isValid(parameters: PlacementParameters): Boolean {
        return anchor.trackingState == TrackingState.TRACKING && plane.trackingState == TrackingState.TRACKING
    }

    override fun update(parameters: PlacementParameters) {
        applyBillboardRotation(parameters.cameraPose, parameters.arGeoObject.node)
    }

    override fun release() = anchor.detach()

    private fun applyBillboardRotation(cameraPose: Pose, node: Node) {
        val deltaX = cameraPose.tx() - node.position.x
        val deltaZ = cameraPose.tz() - node.position.z
        node.worldRotation = ArMath.yawRotation(deltaX, deltaZ)
    }

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
            node.worldRotation = ArMath.wallRotation(normal)

            return AttachedWallState(anchor, plane)
        }
    }
}