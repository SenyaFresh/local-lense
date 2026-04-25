package ru.hse.edu.ar.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.hse.edu.ar.R
import ru.hse.edu.ar.presentation.components.HeadingPickerComposable
import ru.hse.edu.ar.presentation.components.formatHeading
import ru.hse.edu.ar.presentation.mapkit.LocationPickerComposable
import ru.hse.edu.ar.presentation.mapkit.formatCoordinates
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
    initialHeading: Float? = null,
    onHeadingChange: (Float?) -> Unit,
    onContinue: (latitude: Double, longitude: Double) -> Unit,
) {
    var rememberedLat by remember { mutableStateOf<Double?>(null) }
    var rememberedLng by remember { mutableStateOf<Double?>(null) }
    var rememberedHeading by remember { mutableStateOf<Float?>(null) }

    if (rememberedLat == null && initialLatitude != null) {
        rememberedLat = initialLatitude
    }
    if (rememberedLng == null && initialLongitude != null) {
        rememberedLng = initialLongitude
    }
    if (rememberedHeading == null && initialHeading != null) {
        rememberedHeading = normalizeHeading(initialHeading)
    }

    val fixedLat = rememberedLat
    val fixedLng = rememberedLng
    val fixedHeading = rememberedHeading

    if (fixedLat == null || fixedLng == null) {
        OnLoadingEffect()
        return
    }

    var latitude by remember { mutableDoubleStateOf(fixedLat) }
    var longitude by remember { mutableDoubleStateOf(fixedLng) }
    var customHeading by remember { mutableStateOf<Float?>(null) }
    var isMapVisible by remember { mutableStateOf(false) }
    var isHeadingPickerVisible by remember { mutableStateOf(false) }
    var hasCustomLocation by remember { mutableStateOf(false) }

    val displayHeading = customHeading ?: fixedHeading
    val isCustomHeading = customHeading != null

    BackHandler(enabled = isMapVisible || isHeadingPickerVisible) {
        when {
            isHeadingPickerVisible -> isHeadingPickerVisible = false
            isMapVisible -> isMapVisible = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
        ) {
            Text(
                text = stringResource(R.string.ar_preparations_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.ar_preparations_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            CompassCalibrationCard()

            Spacer(modifier = Modifier.height(12.dp))

            HeadingInfoCard(
                heading = displayHeading,
                isCustomHeading = isCustomHeading,
                onChangeClick = { isHeadingPickerVisible = true },
            )

            Spacer(modifier = Modifier.height(12.dp))

            LocationInfoCard(
                latitude = latitude,
                longitude = longitude,
                isCustomLocation = hasCustomLocation,
                onChangeClick = { isMapVisible = true },
            )

            Spacer(modifier = Modifier.weight(1f))

            DefaultPrimaryButton(
                label = stringResource(R.string.ar_preparations_continue),
                onClick = {
                    onHeadingChange(customHeading)
                    onContinue(latitude, longitude)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AnimatedVisibility(
            visible = isMapVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(400, easing = FastOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing),
            ) + fadeOut(animationSpec = tween(250)),
        ) {
            LocationPickerComposable(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                title = stringResource(R.string.ar_preparations_picker_location_title),
                initialLatitude = latitude,
                initialLongitude = longitude,
                onConfirm = { lat, lng ->
                    latitude = lat
                    longitude = lng
                    hasCustomLocation = true
                    isMapVisible = false
                },
                onDismiss = { isMapVisible = false },
            )
        }

        AnimatedVisibility(
            visible = isHeadingPickerVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(400, easing = FastOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing),
            ) + fadeOut(animationSpec = tween(250)),
        ) {
            HeadingPickerComposable(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                title = stringResource(R.string.ar_preparations_picker_heading_title),
                initialHeading = displayHeading ?: 0f,
                onConfirm = { heading ->
                    customHeading = normalizeHeading(heading)
                    isHeadingPickerVisible = false
                },
                onReset = {
                    customHeading = null
                    isHeadingPickerVisible = false
                },
                onDismiss = { isHeadingPickerVisible = false },
            )
        }
    }
}

@Composable
private fun CompassCalibrationCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(18.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Figure8Animation(modifier = Modifier.size(56.dp))
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.ar_preparations_compass_calibration_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.ar_preparations_compass_calibration_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HeadingInfoCard(
    heading: Float?,
    isCustomHeading: Boolean,
    onChangeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = accentColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(14.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(40.dp),
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(
                            if (isCustomHeading) R.string.ar_preparations_heading_custom_title
                            else R.string.ar_preparations_heading_auto_title
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = heading?.let(::formatHeading)
                            ?: stringResource(R.string.ar_preparations_heading_auto_value),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(
                            if (isCustomHeading) R.string.ar_preparations_heading_locked_hint
                            else R.string.ar_preparations_heading_change_hint
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            FilledTonalButton(
                onClick = onChangeClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = accentColor.copy(alpha = 0.12f),
                    contentColor = accentColor,
                ),
            ) {
                Text(
                    text = stringResource(
                        if (isCustomHeading) R.string.ar_preparations_heading_change_action
                        else R.string.ar_preparations_heading_set_action
                    ),
                    fontWeight = FontWeight.Medium,
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

@Composable
private fun LocationInfoCard(
    latitude: Double,
    longitude: Double,
    isCustomLocation: Boolean,
    onChangeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = MaterialTheme.colorScheme.tertiary

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = accentColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(14.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(40.dp),
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(
                            if (isCustomLocation) R.string.ar_preparations_location_custom_title
                            else R.string.ar_preparations_location_current_title
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = formatCoordinates(latitude, longitude),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            FilledTonalButton(
                onClick = onChangeClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = accentColor.copy(alpha = 0.12f),
                    contentColor = accentColor,
                ),
            ) {
                Text(
                    text = stringResource(
                        if (isCustomLocation) R.string.ar_preparations_location_change_action
                        else R.string.ar_preparations_location_set_action
                    ),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

private fun normalizeHeading(heading: Float): Float {
    val normalized = heading % 360f
    return if (normalized < 0f) normalized + 360f else normalized
}

@Preview(showBackground = true)
@Composable
private fun CompassCalibrationCardPreview() {
    MaterialTheme {
        CompassCalibrationCard(modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun HeadingInfoCardPreview() {
    MaterialTheme {
        HeadingInfoCard(
            heading = 86f,
            isCustomHeading = true,
            onChangeClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationInfoCardPreview() {
    MaterialTheme {
        LocationInfoCard(
            latitude = 55.7558,
            longitude = 37.6173,
            isCustomLocation = false,
            onChangeClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}