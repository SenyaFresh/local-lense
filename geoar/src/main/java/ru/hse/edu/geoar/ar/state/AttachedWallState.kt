package ru.hse.edu.geoar.ar.state

import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.TrackingState
import ru.hse.edu.geoar.ar.ArGeoConfig
import ru.hse.edu.geoar.ar.ArMath

class AttachedWallState(
    private val anchor: Anchor,
    private val wallNormal: FloatArray
) : ArPlacementState {

    override fun update(params: PlacementParams): ArPlacementState {
        if (anchor.trackingState != TrackingState.TRACKING) {
            return SearchingState
        }

        val node = params.geoObject.node
        node.worldPosition = ArMath.wallPosition(
            anchor.pose, wallNormal, ArGeoConfig.WALL_OFFSET
        )
        node.worldRotation = ArMath.wallRotation(wallNormal)
        node.isVisible = true

        return this
    }

    override fun release() {
        anchor.detach()
    }

    companion object {

        fun create(hit: HitResult, params: PlacementParams): AttachedWallState {
            val anchor = hit.createAnchor()
            val normal = FloatArray(3)
            hit.hitPose.getTransformedAxis(1, 1f, normal, 0)

            val node = params.geoObject.node
            node.worldPosition = ArMath.wallPosition(
                anchor.pose, normal, ArGeoConfig.WALL_OFFSET
            )
            node.worldRotation = ArMath.wallRotation(normal)
            node.isVisible = true

            return AttachedWallState(anchor, normal)
        }
    }
}