package ru.hse.edu.ar.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.hse.edu.ar.presentation.components.compass.ChipStartPadding
import ru.hse.edu.ar.presentation.components.compass.CompassMarkerChip
import ru.hse.edu.ar.presentation.components.compass.CompassMarkerData
import ru.hse.edu.ar.presentation.components.compass.CompassRail
import ru.hse.edu.ar.presentation.components.compass.VISIBLE_HALF_WINDOW_DEG
import ru.hse.edu.ar.presentation.components.compass.layoutInlineMarkers
import ru.hse.edu.ar.presentation.components.heading.normalizeHeading
import ru.hse.edu.geoar.ar.ArGeoConfig

private val StripHeight = 44.dp
private val PinSize = 14.dp to 19.dp
private val ChipSize = 78.dp to 20.dp
private val RowSpacing = 2.dp
private val PointerHeight = 9.dp
private val PointerGap = 1.dp

data class ArCompassMarkerData(
    val id: Long,
    val color: Color,
    val distanceMeters: Double,
    val screenBearingDegrees: Float,
    val altitudeDeltaMeters: Double,
)

@Composable
fun ArCompassOverlay(
    markers: List<ArCompassMarkerData>,
    userHeading: Float,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val tickLabelStyle = MaterialTheme.typography.labelSmall.copy(
        color = Color.White.copy(alpha = 0.85f),
        fontWeight = FontWeight.Medium,
    )
    val cardinalLabelStyle = MaterialTheme.typography.titleSmall.copy(
        color = colorScheme.primary,
        fontWeight = FontWeight.Bold,
    )

    val normalizedHeading = normalizeHeading(userHeading)
    val visibleMarkers = remember(markers) { markers.toVisibleCompassMarkers() }

    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .padding(horizontal = 6.dp, vertical = 8.dp),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val railWidthPx = with(density) { maxWidth.toPx() }
            val chipWidthPx = with(density) { ChipSize.first.toPx() }
            val pinWidthPx = with(density) { PinSize.first.toPx() }
            val chipStartPaddingPx = with(density) { ChipStartPadding.toPx() }

            val placed = remember(visibleMarkers, railWidthPx) {
                layoutInlineMarkers(visibleMarkers, railWidthPx, chipWidthPx, pinWidthPx)
            }
            val rowsAboveStrip = ((placed.maxOfOrNull { it.row } ?: -1) + 1 - 2)
                .coerceAtLeast(0)
            val totalHeight = StripHeight +
                    (ChipSize.second + RowSpacing) * rowsAboveStrip +
                    PointerHeight + PointerGap

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeight),
            ) {
                CompassRail(
                    normalizedHeading = normalizedHeading,
                    pointerColor = colorScheme.tertiary,
                    textMeasurer = textMeasurer,
                    tickLabelStyle = tickLabelStyle,
                    cardinalLabelStyle = cardinalLabelStyle,
                    stripHeight = StripHeight,
                    pointerHeight = PointerHeight,
                    modifier = Modifier.fillMaxWidth(),
                )

                placed.forEach { marker ->
                    val pinAnchorPx = (marker.xPx - pinWidthPx / 2f - chipStartPaddingPx)
                        .coerceIn(0f, (railWidthPx - chipWidthPx).coerceAtLeast(0f))
                    val rowGap = 2.dp + (ChipSize.second + RowSpacing) * marker.row
                    CompassMarkerChip(
                        data = marker.data,
                        accentColor = colorScheme.tertiary,
                        pinWidthDp = PinSize.first,
                        pinHeightDp = PinSize.second,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset { IntOffset(pinAnchorPx.toInt(), -rowGap.roundToPx()) },
                    )
                }
            }
        }
    }
}

private fun List<ArCompassMarkerData>.toVisibleCompassMarkers(): List<CompassMarkerData> =
    mapNotNull { marker ->
        if (marker.distanceMeters > ArGeoConfig.MAX_DISTANCE_METERS) return@mapNotNull null
        if (marker.screenBearingDegrees < -VISIBLE_HALF_WINDOW_DEG ||
            marker.screenBearingDegrees > VISIBLE_HALF_WINDOW_DEG
        ) return@mapNotNull null
        CompassMarkerData(
            id = marker.id,
            color = marker.color,
            distance = marker.distanceMeters,
            relativeBearing = marker.screenBearingDegrees,
            altitudeDelta = marker.altitudeDeltaMeters,
        )
    }.sortedBy { it.distance }
