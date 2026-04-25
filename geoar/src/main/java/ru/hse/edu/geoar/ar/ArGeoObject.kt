package ru.hse.edu.geoar.ar

import io.github.sceneview.node.Node
import ru.hse.locallense.common.entities.LocationData

data class ArGeoObject(
    val node: Node,
    val locationData: LocationData,
    val isWallAnchor: Boolean,
    val id: Long? = null,
)