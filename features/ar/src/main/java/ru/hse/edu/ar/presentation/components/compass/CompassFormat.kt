package ru.hse.edu.ar.presentation.components.compass

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.abs
import kotlin.math.atan2

internal fun formatDistance(meters: Double): String =
    if (meters < 1000.0) "${meters.toInt()} м"
    else "%.1f км".format(meters / 1000.0)

internal fun cardinalLabel(deg: Int): String = when (deg) {
    0 -> "С"
    90 -> "В"
    180 -> "Ю"
    270 -> "З"
    else -> deg.toString()
}

internal fun altitudeIcon(
    altitudeDelta: Double,
    distanceMeters: Double,
): ImageVector? {
    if (distanceMeters <= 0.0) return null
    val angleDeg = Math.toDegrees(atan2(abs(altitudeDelta), distanceMeters))
    if (angleDeg < ALTITUDE_INDICATOR_THRESHOLD_DEG) return null
    return if (altitudeDelta >= 0.0) Icons.Default.KeyboardArrowUp
    else Icons.Default.KeyboardArrowDown
}
