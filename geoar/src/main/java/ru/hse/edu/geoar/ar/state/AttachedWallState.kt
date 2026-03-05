package ru.hse.edu.geoar.ar.state

import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.TrackingState
import ru.hse.edu.geoar.ar.ArGeoConfig
import ru.hse.edu.geoar.math.ArMath

class AttachedWallState(
    private val anchor: Anchor
) : ArPlacementState {

    override fun update(parameters: PlacementParameters): ArPlacementState {
        if (anchor.trackingState != TrackingState.TRACKING) {
            return SearchingState
        }

        val normal = FloatArray(3)
        anchor.pose.getTransformedAxis(1, 1f, normal, 0)

        val node = parameters.arGeoObject.node
        node.worldPosition = ArMath.wallPosition(
            anchor.pose, normal, ArGeoConfig.WALL_OFFSET
        )
        node.worldRotation = ArMath.wallRotation(normal)
        node.isVisible = true

        return this
    }

    override fun release() {
        anchor.detach()
    }

    companion object {

        fun create(hitResult: HitResult, parameters: PlacementParameters): AttachedWallState {
            val anchor = hitResult.createAnchor()

            val normal = FloatArray(3)
            anchor.pose.getTransformedAxis(1, 1f, normal, 0)

            val node = parameters.arGeoObject.node
            node.worldPosition = ArMath.wallPosition(
                anchor.pose, normal, ArGeoConfig.WALL_OFFSET
            )
            node.worldRotation = ArMath.wallRotation(normal)
            node.isVisible = true

            return AttachedWallState(anchor)
        }
    }
}