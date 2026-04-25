package ru.hse.edu.ar.presentation.components.addplacemark

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.hse.edu.ar.R
import ru.hse.locallense.components.theme.PlacemarkPalette
import ru.hse.locallense.components.theme.RgbChannel
import kotlin.math.roundToInt

@Composable
internal fun PlacemarkColorPicker(
    selectedColor: Color,
    isCustomColor: Boolean,
    isCustomPanelOpen: Boolean,
    red: Float,
    green: Float,
    blue: Float,
    onPresetSelect: (Color) -> Unit,
    onCustomToggle: () -> Unit,
    onChannelChange: (red: Float, green: Float, blue: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PlacemarkSectionLabel(stringResource(R.string.ar_section_color))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlacemarkPalette.Presets.forEach { preset ->
                ColorDot(
                    color = preset,
                    isSelected = !isCustomColor && selectedColor == preset,
                    onClick = { onPresetSelect(preset) },
                )
            }
            RainbowDot(
                activeColor = selectedColor.takeIf { isCustomColor },
                onClick = onCustomToggle,
            )
        }

        AnimatedVisibility(visible = isCustomPanelOpen) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(Color(red, green, blue)),
                    )
                    ChannelSlider(
                        label = "R",
                        value = red,
                        accent = RgbChannel.Red,
                        onValueChange = { onChannelChange(it, green, blue) },
                    )
                    ChannelSlider(
                        label = "G",
                        value = green,
                        accent = RgbChannel.Green,
                        onValueChange = { onChannelChange(red, it, blue) },
                    )
                    ChannelSlider(
                        label = "B",
                        value = blue,
                        accent = RgbChannel.Blue,
                        onValueChange = { onChannelChange(red, green, it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorDot(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) Modifier.border(
                    2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape,
                ) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
            )
        }
    }
}

@Composable
private fun RainbowDot(
    activeColor: Color?,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .then(if (activeColor != null) Modifier.background(activeColor) else Modifier)
            .border(
                width = 2.dp,
                brush = Brush.sweepGradient(
                    listOf(
                        Color.Red, Color.Yellow, Color.Green,
                        Color.Cyan, Color.Blue, Color.Magenta, Color.Red,
                    )
                ),
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (activeColor != null) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (activeColor.luminance() > 0.5f) Color.Black else Color.White,
            )
        }
    }
}

@Composable
private fun ChannelSlider(
    label: String,
    value: Float,
    accent: Color,
    onValueChange: (Float) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
            modifier = Modifier.width(14.dp),
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = accent.copy(alpha = 0.24f),
            ),
        )
        Text(
            text = "${(value * 255).roundToInt()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.End,
        )
    }
}
