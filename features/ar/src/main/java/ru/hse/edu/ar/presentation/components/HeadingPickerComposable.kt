package ru.hse.edu.ar.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun HeadingPickerComposable(
    title: String,
    initialHeading: Float,
    onConfirm: (Float) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentHeading by remember(initialHeading) {
        mutableFloatStateOf(normalizeHeading(initialHeading))
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть",
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )

            TextButton(onClick = onReset) {
                Text(text = "Авто")
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    HeadingPreview(
                        heading = currentHeading,
                        modifier = Modifier.size(220.dp),
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 6.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Slider(
                            value = currentHeading,
                            onValueChange = { currentHeading = normalizeHeading(it) },
                            valueRange = 0f..359f,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "С",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "В",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "Ю",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "З",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilledTonalButton(
                                onClick = { currentHeading = normalizeHeading(currentHeading - 5f) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(text = "−5°")
                            }

                            FilledTonalButton(
                                onClick = { currentHeading = normalizeHeading(currentHeading + 5f) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(text = "+5°")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onConfirm(currentHeading) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = "Подтвердить",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun HeadingPreview(
    heading: Float,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 2.dp.toPx()
            val outerRadius = size.minDimension / 2 - strokeWidth

            drawCircle(
                color = outlineColor,
                radius = outerRadius,
                style = Stroke(width = strokeWidth),
            )

            drawCircle(
                color = primaryColor.copy(alpha = 0.06f),
                radius = outerRadius * 0.72f,
            )

            drawLine(
                color = primaryColor,
                start = Offset(center.x, center.y - outerRadius),
                end = Offset(center.x, center.y - outerRadius * 0.86f),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round,
            )

            repeat(36) { index ->
                val angle = Math.toRadians(index * 10.0 - 90.0)
                val isMajor = index % 9 == 0
                val startRadius = outerRadius * if (isMajor) 0.78f else 0.84f
                val endRadius = outerRadius * 0.94f

                val start = Offset(
                    x = center.x + cos(angle).toFloat() * startRadius,
                    y = center.y + sin(angle).toFloat() * startRadius,
                )
                val end = Offset(
                    x = center.x + cos(angle).toFloat() * endRadius,
                    y = center.y + sin(angle).toFloat() * endRadius,
                )

                drawLine(
                    color = if (isMajor) primaryColor.copy(alpha = 0.45f) else outlineColor,
                    start = start,
                    end = end,
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }

        Text(
            text = "С",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = onSurfaceColor,
        )
        Text(
            text = "В",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = onSurfaceColor,
        )
        Text(
            text = "Ю",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = onSurfaceColor,
        )
        Text(
            text = "З",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = onSurfaceColor,
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(36.dp)
                .graphicsLayer { rotationZ = heading },
        ) {
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
                color = primaryColor,
            )

            drawCircle(
                color = surfaceColor,
                radius = 10.dp.toPx(),
                center = Offset(centerX, centerY),
            )
        }

        Surface(
            shape = CircleShape,
            tonalElevation = 4.dp,
        ) {
            Text(
                text = formatHeadingDegrees(heading),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

fun normalizeHeading(heading: Float): Float {
    val normalized = heading % 360f
    return if (normalized < 0f) normalized + 360f else normalized
}

fun formatHeading(heading: Float): String =
    "${formatHeadingDegrees(heading)} · ${headingDirectionLabel(heading)}"

private fun formatHeadingDegrees(heading: Float): String {
    val degrees = normalizeHeading(heading).roundToInt() % 360
    return "$degrees°"
}

private fun headingDirectionLabel(heading: Float): String {
    val directions = arrayOf("С", "СВ", "В", "ЮВ", "Ю", "ЮЗ", "З", "СЗ")
    val normalized = normalizeHeading(heading)
    val index = ((normalized + 22.5f) / 45f).toInt() % directions.size
    return directions[index]
}