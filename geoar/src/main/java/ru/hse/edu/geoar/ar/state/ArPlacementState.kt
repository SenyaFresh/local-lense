package ru.hse.edu.geoar.ar.state

import com.google.ar.core.Frame
import com.google.ar.core.Pose
import io.github.sceneview.node.Node
import ru.hse.edu.geoar.ar.ArGeoObject
import ru.hse.edu.geoar.math.ArMath
import ru.hse.locallense.common.entities.LocationData

data class PlacementParameters(
    val arGeoObject: ArGeoObject,
    val userLocation: LocationData,
    val userHeading: Float,
    val frame: Frame,
    val cameraPose: Pose,
    val distance: Double,
    val initialCameraHeading: Float,
)

abstract class ArPlacementState {
    protected fun applyBillboardRotation(cameraPose: Pose, node: Node) {
        val deltaX = cameraPose.tx() - node.position.x
        val deltaY = cameraPose.ty() - node.position.y
        val deltaZ = cameraPose.tz() - node.position.z
        node.worldRotation = ArMath.billboardRotation(deltaX, deltaY, deltaZ)
    }

    open fun isValid(parameters: PlacementParameters): Boolean = false
    open fun update(parameters: PlacementParameters) = Unit
    open fun release() = Unit
}