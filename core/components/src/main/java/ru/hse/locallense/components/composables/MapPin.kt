package ru.hse.locallense.components.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MapPin(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.tertiary,
    dotColor: Color = MaterialTheme.colorScheme.onTertiary,
    width: Dp = 34.dp,
    height: Dp = 46.dp,
    drawDot: Boolean = true,
) {
    Canvas(modifier = modifier.size(width, height)) {
        val r = size.width / 2f
        val center = Offset(r, r)

        drawCircle(color, r, center)

        val path = Path().apply {
            moveTo(r - r * 0.45f, r + r * 0.25f)
            lineTo(r, size.height)
            lineTo(r + r * 0.45f, r + r * 0.25f)
            close()
        }
        drawPath(path, color)

        if (drawDot) drawCircle(dotColor, r * 0.3f, center)
    }
}
