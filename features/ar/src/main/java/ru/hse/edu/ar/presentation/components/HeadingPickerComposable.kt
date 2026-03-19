package ru.hse.edu.ar.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Brush
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

    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    contentColor = colorScheme.onSurface,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть",
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatHeading(currentHeading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }

            FilledTonalButton(
                onClick = onReset,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colorScheme.tertiaryContainer,
                    contentColor = colorScheme.onTertiaryContainer,
                ),
            ) {
                Text(text = "Авто")
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(28.dp),
                color = colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colorScheme.primaryContainer.copy(alpha = 0.22f),
                                    colorScheme.surface,
                                    colorScheme.tertiaryContainer.copy(alpha = 0.18f),
                                ),
                            ),
                        ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            HeadingPreview(
                                heading = currentHeading,
                                modifier = Modifier
                                    .fillMaxWidth(0.82f)
                                    .aspectRatio(1f),
                            )
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = colorScheme.surface.copy(alpha = 0.94f),
                            tonalElevation = 6.dp,
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Slider(
                                    value = currentHeading,
                                    onValueChange = { currentHeading = normalizeHeading(it) },
                                    valueRange = 0f..359f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = colorScheme.primary,
                                        activeTrackColor = colorScheme.primary,
                                        inactiveTrackColor = colorScheme.primary.copy(alpha = 0.18f),
                                    ),
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = "С",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colorScheme.tertiary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = "В",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "Ю",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "З",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "С",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colorScheme.tertiary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            currentHeading = normalizeHeading(currentHeading - 5f)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = colorScheme.primaryContainer,
                                            contentColor = colorScheme.onPrimaryContainer,
                                        ),
                                    ) {
                                        Text(text = "−5°")
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            currentHeading = normalizeHeading(currentHeading + 5f)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = colorScheme.secondaryContainer,
                                            contentColor = colorScheme.onSecondaryContainer,
                                        ),
                                    ) {
                                        Text(text = "+5°")
                                    }
                                }
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
                .padding(bottom = 20.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Подтвердить",
                style = MaterialTheme.typography.titleMedium,
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
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 8.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                colorScheme.primaryContainer.copy(alpha = 0.34f),
                                colorScheme.surface,
                                colorScheme.tertiaryContainer.copy(alpha = 0.18f),
                            ),
                        ),
                    ),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
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

                    repeat(36) { index ->
                        val angle = Math.toRadians(index * 10.0 - 90.0)
                        val isMajor = index % 9 == 0
                        val startRadius = outerRadius * if (isMajor) 0.76f else 0.83f
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
                            color = if (isMajor) {
                                colorScheme.primary.copy(alpha = 0.42f)
                            } else {
                                colorScheme.outlineVariant.copy(alpha = 0.9f)
                            },
                            start = start,
                            end = end,
                            strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }

                    drawLine(
                        color = colorScheme.tertiary,
                        start = Offset(center.x, center.y - outerRadius),
                        end = Offset(center.x, center.y - outerRadius * 0.83f),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }

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
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colorScheme.tertiary,
                                colorScheme.primary,
                            ),
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
            }
        }

        Text(
            text = "С",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 18.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.tertiary,
        )

        Text(
            text = "В",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurfaceVariant,
        )

        Text(
            text = "Ю",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurfaceVariant,
        )

        Text(
            text = "З",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurfaceVariant,
        )

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