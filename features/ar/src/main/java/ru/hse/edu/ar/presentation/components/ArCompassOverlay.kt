package ru.hse.edu.ar.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hse.edu.geoar.ar.ArGeoConfig
import ru.hse.locallense.components.composables.MapPin
import ru.hse.locallense.components.theme.PlacemarkPalette
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.floor

private const val VISIBLE_HALF_WINDOW_DEG = 35f
private const val ALTITUDE_INDICATOR_THRESHOLD_DEG = 3.0

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

    val computedMarkers = remember(markers) {
        markers.mapNotNull { marker ->
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
    }

    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .padding(horizontal = 6.dp, vertical = 8.dp),
    ) {
        CompassWithInlineMarkers(
            normalizedHeading = normalizedHeading,
            markers = computedMarkers,
            density = density,
            pointerColor = colorScheme.tertiary,
            accentColor = colorScheme.tertiary,
            textMeasurer = textMeasurer,
            tickLabelStyle = tickLabelStyle,
            cardinalLabelStyle = cardinalLabelStyle,
        )
    }
}

@Composable
private fun CompassWithInlineMarkers(
    normalizedHeading: Float,
    markers: List<CompassMarkerData>,
    density: Density,
    pointerColor: Color,
    accentColor: Color,
    textMeasurer: TextMeasurer,
    tickLabelStyle: TextStyle,
    cardinalLabelStyle: TextStyle,
) {
    val stripHeightDp = 44.dp
    val pinWidthDp = 14.dp
    val pinHeightDp = 19.dp
    val chipHeightDp = 20.dp
    val chipWidthDp = 78.dp
    val rowSpacingDp = 2.dp
    val pointerHeightDp = 9.dp
    val pointerGapDp = 1.dp

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val railWidthPx = with(density) { maxWidth.toPx() }
        val chipWidthPx = with(density) { chipWidthDp.toPx() }
        val pinWidthPx = with(density) { pinWidthDp.toPx() }
        val chipStartPaddingPx = with(density) { chipStartPaddingDp.toPx() }

        val placed = remember(markers, railWidthPx) {
            layoutInlineMarkers(markers, railWidthPx, chipWidthPx, pinWidthPx)
        }
        val rowCount = (placed.maxOfOrNull { it.row } ?: -1) + 1
        val rowsAboveStrip = (rowCount - 2).coerceAtLeast(0)
        val extraRowsDp = (chipHeightDp + rowSpacingDp) * rowsAboveStrip
        val totalHeightDp = stripHeightDp + extraRowsDp + pointerHeightDp + pointerGapDp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeightDp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(stripHeightDp)
                    .align(Alignment.BottomCenter)
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.horizontalGradient(
                                0f to Color.Transparent,
                                0.08f to Color.Black,
                                0.92f to Color.Black,
                                1f to Color.Transparent,
                            ),
                            blendMode = BlendMode.DstIn,
                        )
                    },
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val widthPx = size.width
                    val heightPx = size.height
                    val centerX = widthPx / 2f
                    val baseY = heightPx - 2.dp.toPx()

                    val tickStrokeMajor = 2.dp.toPx()
                    val tickStrokeMinor = 1.dp.toPx()
                    val tickHeightCardinal = 18.dp.toPx()
                    val tickHeightMajor = 14.dp.toPx()
                    val tickHeightMedium = 11.dp.toPx()
                    val tickHeightMinor = 7.dp.toPx()
                    val labelGap = 3.dp.toPx()

                    val startDeg = (floor(normalizedHeading - VISIBLE_HALF_WINDOW_DEG).toInt() / 5) * 5 - 5
                    val endDeg = (ceil(normalizedHeading + VISIBLE_HALF_WINDOW_DEG).toInt() / 5) * 5 + 5

                    for (deg in startDeg..endDeg step 5) {
                        val rel = ((deg.toFloat() - normalizedHeading + 540f) % 360f) - 180f
                        if (rel < -VISIBLE_HALF_WINDOW_DEG || rel > VISIBLE_HALF_WINDOW_DEG) continue

                        val xRatio = rel / VISIBLE_HALF_WINDOW_DEG
                        val x = centerX + xRatio * (widthPx / 2f)

                        val normDeg = ((deg % 360) + 360) % 360
                        val isCardinal = normDeg % 90 == 0
                        val isMajor = !isCardinal && normDeg % 30 == 0
                        val isMedium = !isCardinal && !isMajor && normDeg % 15 == 0

                        val tickHeight = when {
                            isCardinal -> tickHeightCardinal
                            isMajor -> tickHeightMajor
                            isMedium -> tickHeightMedium
                            else -> tickHeightMinor
                        }
                        val tickColor = when {
                            isCardinal -> Color.White
                            isMajor -> Color.White.copy(alpha = 0.9f)
                            else -> Color.White.copy(alpha = 0.55f)
                        }
                        val tickStroke = if (isCardinal || isMajor) tickStrokeMajor else tickStrokeMinor

                        drawLine(
                            color = tickColor,
                            start = Offset(x, baseY),
                            end = Offset(x, baseY - tickHeight),
                            strokeWidth = tickStroke,
                            cap = StrokeCap.Round,
                        )

                        if (isCardinal || isMajor) {
                            val labelText = if (isCardinal) cardinalLabel(normDeg) else normDeg.toString()
                            val style = if (isCardinal) cardinalLabelStyle else tickLabelStyle
                            val layout = textMeasurer.measure(AnnotatedString(labelText), style)
                            val labelX = x - layout.size.width / 2f
                            val labelY = baseY - tickHeight - layout.size.height - labelGap
                            drawText(textLayoutResult = layout, topLeft = Offset(labelX, labelY))
                        }
                    }
                }

            }

            Canvas(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(width = 14.dp, height = 9.dp),
            ) {
                val path = Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(size.width, 0f)
                    lineTo(0f, 0f)
                    close()
                }
                drawPath(path = path, color = pointerColor)
            }

            placed.forEach { p ->
                val pinAnchorPx = (p.xPx - pinWidthPx / 2f - chipStartPaddingPx)
                    .coerceIn(0f, (railWidthPx - chipWidthPx).coerceAtLeast(0f))
                val xOffsetInt = pinAnchorPx.toInt()
                val rowGapDp = 2.dp + (chipHeightDp + rowSpacingDp) * p.row
                MarkerChip(
                    data = p.data,
                    accentColor = accentColor,
                    pinWidthDp = pinWidthDp,
                    pinHeightDp = pinHeightDp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset { IntOffset(xOffsetInt, -rowGapDp.roundToPx()) },
                )
            }
        }
    }
}

private val chipStartPaddingDp = 3.dp

@Composable
private fun MarkerChip(
    data: CompassMarkerData,
    accentColor: Color,
    pinWidthDp: Dp,
    pinHeightDp: Dp,
    modifier: Modifier = Modifier,
) {
    val verticalIcon = altitudeIcon(
        altitudeDelta = data.altitudeDelta,
        distanceMeters = data.distance,
    )
    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
            .padding(start = chipStartPaddingDp, end = 6.dp, top = 1.dp, bottom = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MapPin(
            color = data.color,
            dotColor = Color.White,
            width = pinWidthDp,
            height = pinHeightDp,
            drawDot = true,
        )
        Spacer(Modifier.width(3.dp))
        if (verticalIcon != null) {
            Icon(
                imageVector = verticalIcon,
                contentDescription = null,
                modifier = Modifier.size(11.dp),
                tint = accentColor,
            )
            Spacer(Modifier.width(1.dp))
        }
        Text(
            text = formatDistance(data.distance),
            style = TextStyle(
                color = Color.White,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            maxLines = 1,
            softWrap = false,
        )
    }
}

private data class CompassMarkerData(
    val id: Long,
    val color: Color,
    val distance: Double,
    val relativeBearing: Float,
    val altitudeDelta: Double,
)

private data class PlacedMarker(
    val data: CompassMarkerData,
    val xPx: Float,
    val row: Int,
)

private fun layoutInlineMarkers(
    items: List<CompassMarkerData>,
    railWidthPx: Float,
    chipWidthPx: Float,
    pinWidthPx: Float,
): List<PlacedMarker> {
    val result = mutableListOf<PlacedMarker>()
    val halfWidth = railWidthPx / 2f

    for (item in items) {
        val rel = item.relativeBearing
        val x = halfWidth + (rel / VISIBLE_HALF_WINDOW_DEG) * halfWidth
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

private fun altitudeIcon(
    altitudeDelta: Double,
    distanceMeters: Double,
): ImageVector? {
    if (distanceMeters <= 0.0) return null
    val angleDeg = Math.toDegrees(atan2(abs(altitudeDelta), distanceMeters))
    if (angleDeg < ALTITUDE_INDICATOR_THRESHOLD_DEG) return null
    return if (altitudeDelta >= 0.0) Icons.Default.KeyboardArrowUp
    else Icons.Default.KeyboardArrowDown
}

private fun formatDistance(meters: Double): String =
    if (meters < 1000.0) "${meters.toInt()} м"
    else "%.1f км".format(meters / 1000.0)

private fun cardinalLabel(deg: Int): String = when (deg) {
    0 -> "С"
    90 -> "В"
    180 -> "Ю"
    270 -> "З"
    else -> deg.toString()
}

@Preview(showBackground = true, backgroundColor = 0xFF333333, name = "Compass · spread")
@Composable
private fun PreviewCompassSpread() {
    ArCompassOverlay(
        markers = listOf(
            previewMarker(1L, PlacemarkPalette.Default, distance = 25.0, bearing = -28f, alt = 5.0),
            previewMarker(2L, Color(0xFF00C853), distance = 110.0, bearing = -10f, alt = -3.0),
            previewMarker(3L, Color(0xFFFF6D00), distance = 78.0, bearing = 6f, alt = 0.5),
            previewMarker(4L, Color(0xFF2962FF), distance = 230.0, bearing = 24f, alt = 0.2),
        ),
        userHeading = 35f,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF333333, name = "Compass · empty")
@Composable
private fun PreviewCompassEmpty() {
    ArCompassOverlay(
        markers = emptyList(),
        userHeading = 0f,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF333333, name = "Compass · stack overlap")
@Composable
private fun PreviewCompassStack() {
    ArCompassOverlay(
        markers = listOf(
            previewMarker(1L, PlacemarkPalette.Default, distance = 60.0, bearing = -1f, alt = 4.0),
            previewMarker(2L, Color(0xFF00C853), distance = 65.0, bearing = 1f, alt = -5.0),
            previewMarker(3L, Color(0xFFFF6D00), distance = 90.0, bearing = 3f),
        ),
        userHeading = 0f,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
    )
}

private fun previewMarker(
    id: Long,
    color: Color,
    distance: Double,
    bearing: Float,
    alt: Double = 0.0,
) = ArCompassMarkerData(
    id = id,
    color = color,
    distanceMeters = distance,
    screenBearingDegrees = bearing,
    altitudeDeltaMeters = alt,
)
