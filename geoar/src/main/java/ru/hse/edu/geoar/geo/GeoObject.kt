package ru.hse.edu.geoar.geo

import io.github.sceneview.node.Node

data class GeoObject(
    val node: Node,
    val latitude: Double,
    val longitude: Double
)