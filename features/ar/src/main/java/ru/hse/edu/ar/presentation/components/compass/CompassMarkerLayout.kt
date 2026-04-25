package ru.hse.edu.ar.presentation.components.compass

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal const val VISIBLE_HALF_WINDOW_DEG = 35f
internal const val ALTITUDE_INDICATOR_THRESHOLD_DEG = 3.0
internal val ChipStartPadding = 3.dp

internal data class CompassMarkerData(
    val id: Long,
    val color: Color,
    val distance: Double,
    val relativeBearing: Float,
    val altitudeDelta: Double,
)

internal data class PlacedMarker(
    val data: CompassMarkerData,
    val xPx: Float,
    val row: Int,
)

internal fun layoutInlineMarkers(
    items: List<CompassMarkerData>,
    railWidthPx: Float,
    chipWidthPx: Float,
    pinWidthPx: Float,
): List<PlacedMarker> {
    val result = mutableListOf<PlacedMarker>()
    val halfWidth = railWidthPx / 2f

    for (item in items) {
        val x = halfWidth + (item.relativeBearing / VISIBLE_HALF_WINDOW_DEG) * halfWidth
        val leftEdge = x - pinWidthPx / 2f
        val rightEdge = leftEdge + chipWidthPx
        var row = 0
        while (
            result.any { other ->
                if (other.row != row) return@any false
                val otherLeft = other.xPx - pinWidthPx / 2f
                val otherRight = otherLeft + chipWidthPx
                !(rightEdge <= otherLeft || leftEdge >= otherRight)
            }
        ) {
            row++
        }
        result += PlacedMarker(item, x, row)
    }
    return result
}
