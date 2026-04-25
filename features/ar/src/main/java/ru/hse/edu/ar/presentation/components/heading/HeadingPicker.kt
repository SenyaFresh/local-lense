package ru.hse.edu.ar.presentation.components.heading

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.hse.edu.ar.R

@Composable
fun HeadingPicker(
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
        HeadingPickerHeader(
            title = title,
            currentHeading = currentHeading,
            onDismiss = onDismiss,
            onReset = onReset,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            HeadingPickerSurface(
                currentHeading = currentHeading,
                onHeadingChange = { currentHeading = normalizeHeading(it) },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ConfirmButton(onClick = { onConfirm(currentHeading) })
    }
}

@Composable
private fun HeadingPickerHeader(
    title: String,
    currentHeading: Float,
    onDismiss: () -> Unit,
    onReset: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
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
                contentDescription = stringResource(R.string.ar_action_close_cd),
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
            Text(text = stringResource(R.string.ar_heading_picker_auto))
        }
    }
}

@Composable
private fun HeadingPickerSurface(
    currentHeading: Float,
    onHeadingChange: (Float) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(28.dp),
        color = colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = verticalSurfaceGradient(colorScheme)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    HeadingCompass(
                        heading = currentHeading,
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .aspectRatio(1f),
                    )
                }
                HeadingSliderControls(
                    currentHeading = currentHeading,
                    onHeadingChange = onHeadingChange,
                )
            }
        }
    }
}

private fun verticalSurfaceGradient(colorScheme: ColorScheme): Brush =
    Brush.verticalGradient(
        colors = listOf(
            colorScheme.primaryContainer.copy(alpha = 0.22f),
            colorScheme.surface,
            colorScheme.tertiaryContainer.copy(alpha = 0.18f),
        ),
    )

@Composable
private fun HeadingSliderControls(
    currentHeading: Float,
    onHeadingChange: (Float) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
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
                onValueChange = onHeadingChange,
                valueRange = 0f..359f,
                colors = SliderDefaults.colors(
                    thumbColor = colorScheme.primary,
                    activeTrackColor = colorScheme.primary,
                    inactiveTrackColor = colorScheme.primary.copy(alpha = 0.18f),
                ),
            )

            CardinalScaleLabels()

            HeadingNudgeButtons(
                onNudge = { delta -> onHeadingChange(normalizeHeading(currentHeading + delta)) },
            )
        }
    }
}

@Composable
private fun CardinalScaleLabels() {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ScaleLabel(R.string.ar_compass_cardinal_north, accent = true, colorScheme)
        ScaleLabel(R.string.ar_compass_cardinal_east, accent = false, colorScheme)
        ScaleLabel(R.string.ar_compass_cardinal_south, accent = false, colorScheme)
        ScaleLabel(R.string.ar_compass_cardinal_west, accent = false, colorScheme)
        ScaleLabel(R.string.ar_compass_cardinal_north, accent = true, colorScheme)
    }
}

@Composable
private fun ScaleLabel(textRes: Int, accent: Boolean, colorScheme: ColorScheme) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.labelMedium,
        color = if (accent) colorScheme.tertiary else colorScheme.onSurfaceVariant,
        fontWeight = if (accent) FontWeight.SemiBold else FontWeight.Normal,
    )
}

@Composable
private fun HeadingNudgeButtons(onNudge: (Float) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FilledTonalButton(
            onClick = { onNudge(-5f) },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = colorScheme.primaryContainer,
                contentColor = colorScheme.onPrimaryContainer,
            ),
        ) {
            Text(text = stringResource(R.string.ar_heading_picker_minus_5))
        }
        FilledTonalButton(
            onClick = { onNudge(5f) },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = colorScheme.secondaryContainer,
                contentColor = colorScheme.onSecondaryContainer,
            ),
        ) {
            Text(text = stringResource(R.string.ar_heading_picker_plus_5))
        }
    }
}

@Composable
private fun ConfirmButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
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
            text = stringResource(R.string.ar_action_confirm),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
