package ru.hse.edu.ar.presentation.components.heading

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.hse.edu.ar.R
import kotlin.math.cos
import kotlin.math.sin

private const val TICK_COUNT = 36
private const val TICK_DEGREE_STEP = 10
private const val MAJOR_TICK_STRIDE = 9

@Composable
internal fun HeadingCompass(
    heading: Float,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 8.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = radialBackground(colorScheme)),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawDial(colorScheme)
                }
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(36.dp)
                        .graphicsLayer { rotationZ = heading },
                ) {
                    drawArrow(colorScheme)
                }
            }
        }

        CardinalLabels(colorScheme = colorScheme)

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            Text(
                text = formatHeadingDegrees(heading),
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
            )
        }
    }
}

private fun radialBackground(colorScheme: ColorScheme): Brush =
    Brush.radialGradient(
        colors = listOf(
            colorScheme.primaryContainer.copy(alpha = 0.34f),
            colorScheme.surface,
            colorScheme.tertiaryContainer.copy(alpha = 0.18f),
        ),
    )

private fun DrawScope.drawDial(colorScheme: ColorScheme) {
    val strokeWidth = 2.dp.toPx()
    val outerRadius = size.minDimension / 2 - strokeWidth - 8.dp.toPx()

    drawCircle(
        color = colorScheme.outlineVariant.copy(alpha = 0.7f),
        radius = outerRadius,
        style = Stroke(width = strokeWidth),
    )
    drawCircle(
        color = colorScheme.primary.copy(alpha = 0.08f),
        radius = outerRadius * 0.72f,
    )
    drawCircle(
        color = colorScheme.tertiary.copy(alpha = 0.08f),
        radius = outerRadius * 0.46f,
    )

    repeat(TICK_COUNT) { index ->
        val angle = Math.toRadians(index * TICK_DEGREE_STEP.toDouble() - 90.0)
        if (index % MAJOR_TICK_STRIDE == 0) return@repeat
        val startRadius = outerRadius * 0.83f
        val endRadius = outerRadius * 0.93f

        val start = Offset(
            x = center.x + cos(angle).toFloat() * startRadius,
            y = center.y + sin(angle).toFloat() * startRadius,
        )
        val end = Offset(
            x = center.x + cos(angle).toFloat() * endRadius,
            y = center.y + sin(angle).toFloat() * endRadius,
        )

        drawLine(
            color = colorScheme.outlineVariant.copy(alpha = 0.9f),
            start = start,
            end = end,
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawArrow(colorScheme: ColorScheme) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val tipY = size.height * 0.08f
    val baseY = size.height * 0.56f
    val tailBottom = size.height * 0.86f
    val halfArrowWidth = size.width * 0.10f
    val tailHalfWidth = size.width * 0.03f

    val path = Path().apply {
        moveTo(centerX, tipY)
        lineTo(centerX + halfArrowWidth, baseY)
        lineTo(centerX + tailHalfWidth, baseY)
        lineTo(centerX + tailHalfWidth, tailBottom)
        lineTo(centerX - tailHalfWidth, tailBottom)
        lineTo(centerX - tailHalfWidth, baseY)
        lineTo(centerX - halfArrowWidth, baseY)
        close()
    }

    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(colorScheme.tertiary, colorScheme.primary),
        ),
    )

    drawCircle(
        color = colorScheme.surface,
        radius = 12.dp.toPx(),
        center = Offset(centerX, centerY),
    )
    drawCircle(
        color = colorScheme.primary,
        radius = 4.dp.toPx(),
        center = Offset(centerX, centerY),
    )
}

@Composable
private fun BoxScope.CardinalLabels(colorScheme: ColorScheme) {
    Text(
        text = stringResource(R.string.ar_compass_cardinal_north),
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = colorScheme.tertiary,
    )
    Text(
        text = stringResource(R.string.ar_compass_cardinal_east),
        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = colorScheme.onSurfaceVariant,
    )
    Text(
        text = stringResource(R.string.ar_compass_cardinal_south),
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = colorScheme.onSurfaceVariant,
    )
    Text(
        text = stringResource(R.string.ar_compass_cardinal_west),
        modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = colorScheme.onSurfaceVariant,
    )
}
