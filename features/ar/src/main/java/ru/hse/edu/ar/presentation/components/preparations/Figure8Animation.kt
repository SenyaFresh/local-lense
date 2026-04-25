package ru.hse.edu.ar.presentation.components.preparations

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val LOOP_DURATION_MS = 3000
private const val PATH_STEPS = 200
private const val TRAIL_COUNT = 20
private const val TRAIL_PROGRESS_DELTA = 0.006f
private const val ELLIPSE_RX_RATIO = 0.28f
private const val ELLIPSE_RY_RATIO = 0.4f

@Composable
internal fun Figure8Animation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "figure8")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = LOOP_DURATION_MS, easing = LinearEasing),
        ),
        label = "progress",
    )

    val pathColor = MaterialTheme.colorScheme.outlineVariant
    val accentColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val cx = size.width / 2
        val cy = size.height / 2
        val rx = size.width * ELLIPSE_RX_RATIO
        val ry = size.height * ELLIPSE_RY_RATIO

        drawFigure8Path(cx, cy, rx, ry, pathColor)
        drawTrail(cx, cy, rx, ry, progress, accentColor)
        drawArrow(cx, cy, rx, ry, progress, accentColor)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFigure8Path(
    cx: Float, cy: Float, rx: Float, ry: Float, color: Color,
) {
    for (i in 0 until PATH_STEPS) {
        val t1 = i.toDouble() / PATH_STEPS * 2.0 * PI
        val t2 = (i + 1).toDouble() / PATH_STEPS * 2.0 * PI
        drawLine(
            color = color,
            start = Offset(cx + (sin(2.0 * t1) * rx).toFloat(), cy - (sin(t1) * ry).toFloat()),
            end = Offset(cx + (sin(2.0 * t2) * rx).toFloat(), cy - (sin(t2) * ry).toFloat()),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTrail(
    cx: Float, cy: Float, rx: Float, ry: Float, progress: Float, accent: Color,
) {
    for (i in TRAIL_COUNT downTo 1) {
        val tp = (progress - i * TRAIL_PROGRESS_DELTA).mod(1f)
        val t = tp.toDouble() * 2.0 * PI
        val alpha = 1f - i.toFloat() / TRAIL_COUNT
        drawCircle(
            color = accent.copy(alpha = alpha * 0.7f),
            radius = 3.dp.toPx() * (0.4f + 0.6f * alpha),
            center = Offset(cx + (sin(2.0 * t) * rx).toFloat(), cy - (sin(t) * ry).toFloat()),
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrow(
    cx: Float, cy: Float, rx: Float, ry: Float, progress: Float, accent: Color,
) {
    val mainT = progress.toDouble() * 2.0 * PI
    val mainPos = Offset(
        cx + (sin(2.0 * mainT) * rx).toFloat(),
        cy - (sin(mainT) * ry).toFloat(),
    )
    val dx = cos(2.0 * mainT) * 2.0 * rx
    val dy = -cos(mainT) * ry
    val angle = atan2(dy, dx)

    rotate(degrees = Math.toDegrees(angle).toFloat() + 90f, pivot = mainPos) {
        val pw = 10.dp.toPx()
        val ph = 16.dp.toPx()
        drawRoundRect(
            color = accent,
            topLeft = Offset(mainPos.x - pw / 2, mainPos.y - ph / 2),
            size = Size(pw, ph),
            cornerRadius = CornerRadius(2.5.dp.toPx()),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.35f),
            topLeft = Offset(
                mainPos.x - pw / 2 + 1.5.dp.toPx(),
                mainPos.y - ph / 2 + 2.5.dp.toPx(),
            ),
            size = Size(pw - 3.dp.toPx(), ph - 5.dp.toPx()),
            cornerRadius = CornerRadius(1.dp.toPx()),
        )
    }
}
