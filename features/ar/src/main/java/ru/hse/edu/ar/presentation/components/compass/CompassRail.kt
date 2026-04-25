package ru.hse.edu.ar.presentation.components.compass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor

private const val TICK_DEGREE_STEP = 5

@Composable
internal fun CompassRail(
    normalizedHeading: Float,
    pointerColor: Color,
    textMeasurer: TextMeasurer,
    tickLabelStyle: TextStyle,
    cardinalLabelStyle: TextStyle,
    stripHeight: Dp,
    pointerHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(stripHeight)
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
                drawCompassTicks(
                    normalizedHeading = normalizedHeading,
                    textMeasurer = textMeasurer,
                    tickLabelStyle = tickLabelStyle,
                    cardinalLabelStyle = cardinalLabelStyle,
                )
            }
        }

        PointerArrow(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = 14.dp, height = pointerHeight),
            color = pointerColor,
        )
    }
}

@Composable
private fun PointerArrow(
    modifier: Modifier,
    color: Color,
) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width / 2f, size.height)
            lineTo(size.width, 0f)
            lineTo(0f, 0f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

private fun DrawScope.drawCompassTicks(
    normalizedHeading: Float,
    textMeasurer: TextMeasurer,
    tickLabelStyle: TextStyle,
    cardinalLabelStyle: TextStyle,
) {
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

    val startDeg = (floor(normalizedHeading - VISIBLE_HALF_WINDOW_DEG).toInt() / TICK_DEGREE_STEP) * TICK_DEGREE_STEP - TICK_DEGREE_STEP
    val endDeg = (ceil(normalizedHeading + VISIBLE_HALF_WINDOW_DEG).toInt() / TICK_DEGREE_STEP) * TICK_DEGREE_STEP + TICK_DEGREE_STEP

    for (deg in startDeg..endDeg step TICK_DEGREE_STEP) {
        val rel = ((deg.toFloat() - normalizedHeading + 540f) % 360f) - 180f
        if (rel < -VISIBLE_HALF_WINDOW_DEG || rel > VISIBLE_HALF_WINDOW_DEG) continue

        val x = centerX + (rel / VISIBLE_HALF_WINDOW_DEG) * (widthPx / 2f)

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
