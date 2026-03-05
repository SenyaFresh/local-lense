package ru.hse.edu.geoar.ar

import io.github.sceneview.node.Node

data class ArGeoObject(
    val node: Node,
    val latitude: Double,
    val longitude: Double,
    val isWallAnchor: Boolean,
)