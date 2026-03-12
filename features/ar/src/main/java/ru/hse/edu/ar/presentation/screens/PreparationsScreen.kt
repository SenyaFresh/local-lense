package ru.hse.edu.ar.presentation.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.hse.edu.ar.di.ArDiContainer
import ru.hse.edu.ar.di.rememberArDiContainer
import ru.hse.edu.ar.presentation.mapkit.LocationPickerComposable
import ru.hse.edu.ar.presentation.viewmodels.ArViewModel
import ru.hse.locallense.components.composables.buttons.DefaultPrimaryButton
import ru.hse.locallense.presentation.OnLoadingEffect
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PreparationsScreen(
    initialLatitude: Double?,
    initialLongitude: Double?,
    onContinue: (latitude: Double, longitude: Double) -> Unit,
) {
    PreparationsContent(
        initialLatitude = initialLatitude,
        initialLongitude = initialLongitude,
        onContinue = onContinue,
    )
}

@Composable
fun PreparationsContent(
    initialLatitude: Double?,
    initialLongitude: Double?,
    onContinue: (latitude: Double, longitude: Double) -> Unit,
) {
    var rememberedLat by remember { mutableStateOf<Double?>(null) }
    var rememberedLng by remember { mutableStateOf<Double?>(null) }

    if (rememberedLat == null && initialLatitude != null) {
        rememberedLat = initialLatitude
    }
    if (rememberedLng == null && initialLongitude != null) {
        rememberedLng = initialLongitude
    }

    val fixedLat = rememberedLat
    val fixedLng = rememberedLng

    if (fixedLat == null || fixedLng == null) {
        OnLoadingEffect()
        return
    }

    var latitude by remember { mutableDoubleStateOf(fixedLat) }
    var longitude by remember { mutableDoubleStateOf(fixedLng) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Подготовка",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        CompassCalibrationCard()

        LocationPickerComposable(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(MaterialTheme.shapes.large),
            title = "Уточните местоположение",
            initialLatitude = fixedLat,
            initialLongitude = fixedLng,
            onConfirm = { lat, lng ->
                latitude = lat
                longitude = lng
            },
        )

        DefaultPrimaryButton(
            label = "Продолжить",
            onClick = { onContinue(latitude, longitude) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CompassCalibrationCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Figure8Animation(modifier = Modifier.size(80.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Калибровка компаса",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Покрутите телефон восьмёркой для повышения точности",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Figure8Animation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "figure8")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
        ),
        label = "progress",
    )

    val pathColor = MaterialTheme.colorScheme.outlineVariant
    val accentColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val cx = size.width / 2
        val cy = size.height / 2
        val rx = size.width * 0.28f
        val ry = size.height * 0.4f

        val steps = 200
        for (i in 0 until steps) {
            val t1 = i.toDouble() / steps * 2.0 * PI
            val t2 = (i + 1).toDouble() / steps * 2.0 * PI
            drawLine(
                color = pathColor,
                start = Offset(
                    cx + (sin(2.0 * t1) * rx).toFloat(),
                    cy - (sin(t1) * ry).toFloat(),
                ),
                end = Offset(
                    cx + (sin(2.0 * t2) * rx).toFloat(),
                    cy - (sin(t2) * ry).toFloat(),
                ),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        val trailCount = 20
        for (i in trailCount downTo 1) {
            val tp = (progress - i * 0.006f).mod(1f)
            val t = tp.toDouble() * 2.0 * PI
            val alpha = 1f - i.toFloat() / trailCount
            drawCircle(
                color = accentColor.copy(alpha = alpha * 0.7f),
                radius = 3.dp.toPx() * (0.4f + 0.6f * alpha),
                center = Offset(
                    cx + (sin(2.0 * t) * rx).toFloat(),
                    cy - (sin(t) * ry).toFloat(),
                ),
            )
        }

        val mainT = progress.toDouble() * 2.0 * PI
        val mainPos = Offset(
            cx + (sin(2.0 * mainT) * rx).toFloat(),
            cy - (sin(mainT) * ry).toFloat(),
        )
        val dx = cos(2.0 * mainT) * 2.0 * rx
        val dy = -cos(mainT) * ry
        val angle = atan2(dy, dx)

        rotate(
            degrees = Math.toDegrees(angle).toFloat() + 90f,
            pivot = mainPos,
        ) {
            val pw = 10.dp.toPx()
            val ph = 16.dp.toPx()
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(mainPos.x - pw / 2, mainPos.y - ph / 2),
                size = Size(pw, ph),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5.dp.toPx()),
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.35f),
                topLeft = Offset(
                    mainPos.x - pw / 2 + 1.5.dp.toPx(),
                    mainPos.y - ph / 2 + 2.5.dp.toPx(),
                ),
                size = Size(pw - 3.dp.toPx(), ph - 5.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx()),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompassCalibrationCardPreview() {
    MaterialTheme {
        CompassCalibrationCard(modifier = Modifier.padding(16.dp))
    }
}